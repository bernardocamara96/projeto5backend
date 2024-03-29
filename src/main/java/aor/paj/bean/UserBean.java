package aor.paj.bean;

import aor.paj.dao.CategoryDao;
import aor.paj.dao.TaskDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.LoginDto;
import aor.paj.dto.User;
import aor.paj.dto.UserInfoCard;
import aor.paj.dto.UserWithNoPassword;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import aor.paj.service.status.userRoleManager;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import util.HashUtil;




import java.io.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * UserBean is a managed bean responsible for managing user data within the application. It provides functionality
 * to read and write user information from and to a JSON file, 'allUser.json'. This bean supports operations such as
 * user registration, login verification, retrieving user information by username, and converting User objects to
 * UserWithNoPassword objects for security purposes. It also includes methods to check user existence, update user
 * information, and manage user passwords. UserBean ensures that user data is persistently stored and accessible
 * throughout the application lifecycle.
 */

@Stateless
public class UserBean implements Serializable {
    @EJB
    UserDao userDao;
    @EJB
    TaskDao taskDao;
    @EJB
    CategoryDao categoryDao;

    private UserEntity convertUserDtotoUserEntity(User user){
        UserEntity userEntity = new UserEntity();
        if(userEntity != null){
            userEntity.setUsername(user.getUsername());
            userEntity.setPassword(user.getPassword());
            userEntity.setEmail(user.getEmail());
            userEntity.setFirstName(user.getFirstName());
            userEntity.setLastName(user.getLastName());
            userEntity.setPhoneNumber(user.getPhoneNumber());
            userEntity.setToken(null);
            userEntity.setPhotoURL(user.getPhotoURL());
            userEntity.setRole(user.getRole());
            userEntity.setConfirmed(user.isConfirmed());
            System.out.println(userEntity);
            System.out.println(user);
            return userEntity;
        }
        return null;
    }


    public boolean register(User user){
        if(user == null) return false;
        else {
            if(user.getRole() == null){
                user.setRole(userRoleManager.DEVELOPER);
                user.setConfirmed(false);
            }
        }
        try {
            userDao.persist(convertUserDtotoUserEntity(user));
            return true;
        } catch (NoResultException e) {
            return false;
        }
    }
    public boolean checkIfUserExists(User user){
        if(user != null){
            if(user.getUsername() !=null){
                return userDao.findUserByUsername(user.getUsername()) != null;
            }
        }
        return false;
    }
    public boolean checkIfemailExists(User user){
        if(user != null){
            if(user.getEmail() !=null){
                return userDao.findUserByEmail(user.getEmail()) != null;
            }
        }
        return false;
    }


    public boolean checkIfUserExists(String username){
        UserEntity user = getUserByUsername(username);
        if(user != null){
            if(userDao.findUserByUsername(user.getUsername()) != null ||
                    userDao.findUserByEmail(user.getEmail()) != null){
                return true;
            }
        }
        return false;
    }
    public String login(LoginDto user){
        System.out.println(user.getUsername());
        UserEntity userEntity = userDao.findUserByUsername(user.getUsername());
        if(userEntity !=null){
            if(!userEntity.getDeleted()){
                System.out.println(userEntity);
                if (userEntity != null){
                    if (userEntity.getPassword().equals(user.getPassword())){
                        String token = generateNewToken();
                        userEntity.setToken(token);
                        return token;
                    }
                }
            }
        }
        return null;
    }
    public boolean tokenValidator(String token ){
        if (userDao.findUserByToken(token) != null && token !=null)
            return true;
        return false;
    }

    public UserEntity getUserByToken(String token){
        UserEntity user=userDao.findUserByToken(token);
        if(user!=null) return user;
        else return null;

    }

    public UserEntity getUserByUsername(String username){
       return userDao.findUserByUsername(username);
    }
    public boolean UserExistsByUsername(String username){
        return userDao.findUserByUsername(username) != null;
    }

    private String generateNewToken() {
        SecureRandom secureRandom = new SecureRandom();
        Base64.Encoder base64Encoder = Base64.getUrlEncoder();
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
    public String getPhotoURLByUsername(String token){
         UserEntity user = userDao.findUserByToken(token);
         return user.getPhotoURL();
    }

    public String getFirstNameByToken(String token){
        UserEntity user = userDao.findUserByToken(token);
      return user.getFirstName();
    }
    public String getRoleByToken(String token){
        UserEntity user = userDao.findUserByToken(token);
        return user.getRole();
    }
    public User convertUserEntityToUser(UserEntity userEntity){
        return new User(userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.getPhoneNumber(),
                userEntity.getEmail(),
                userEntity.getFirstName(),
                userEntity.getLastName(),
                userEntity.getPhotoURL(),
                userEntity.getRole(),
                userEntity.getDeleted(),
                userEntity.isConfirmed());
    }
    public void createDefaultUsersIfNotExistent(){
        UserEntity productOwner = userDao.findUserByUsername("admin");
        UserEntity scrumMaster =userDao.findUserByUsername("scrumMasterTest");
        UserEntity developer =userDao.findUserByUsername("developerTest");
        UserEntity deletedTasks =userDao.findUserByUsername("deletedTasks");
        String hashedAdminPassword = HashUtil.toSHA256("admin");
        if(productOwner == null){
            userDao.persist(new UserEntity("admin", hashedAdminPassword,"admin@admin.com",
                    "admin", "admin", "admin",
                    "https://icons.veryicon.com/png/o/miscellaneous/yuanql/icon-admin.png",
                    "admin", userRoleManager.PRODUCT_OWNER,false,true));
        }
        if(scrumMaster == null){
            userDao.persist(new UserEntity("scrumMasterTest", hashedAdminPassword,"srummaster@admin.com",
                    "scrumMasterTest", "test", "123123123",
                    "https://icons.veryicon.com/png/o/miscellaneous/yuanql/icon-admin.png",
                    "scrum", userRoleManager.SCRUM_MASTER,false,true));
        }
        if(developer == null){
            userDao.persist(new UserEntity("developerTest", hashedAdminPassword,"developer@admin.com",
                    "DeveloperTest", "test", "123123123",
                    "https://icons.veryicon.com/png/o/miscellaneous/yuanql/icon-admin.png",
                    "devel", userRoleManager.DEVELOPER,false,true));
        }
        if(deletedTasks == null){
            userDao.persist(new UserEntity("deletedTasks", hashedAdminPassword,"deleted@admin.com",
                    "deleted", "tasks", "deleted",
                    "https://icons.veryicon.com/png/o/miscellaneous/yuanql/icon-admin.png",
                    "deleted", userRoleManager.PRODUCT_OWNER,false,true));
        }
    }

    public UserWithNoPassword convertUserEntityToUserWithNoPassword(UserEntity userEntity){
      return new UserWithNoPassword(userEntity.getUsername(),
              userEntity.getPhoneNumber(), // Corrigido: phoneNumber antes de email
              userEntity.getEmail(),
              userEntity.getFirstName(),
              userEntity.getLastName(),
              userEntity.getPhotoURL(),
              userEntity.getRole(),
              userEntity.getDeleted());
    }
    public boolean updateUser(String token, User updatedUser) {
        UserEntity user = getUserByToken(token);
        System.out.println(user);
        System.out.println(token);
        if(user != null){
            if(updatedUser.getPhoneNumber() != null) user.setPhoneNumber(updatedUser.getPhoneNumber());
            if(updatedUser.getEmail() != null) user.setEmail(updatedUser.getEmail());
            if(updatedUser.getFirstName() != null) user.setFirstName(updatedUser.getFirstName());
            if(updatedUser.getLastName() != null) user.setLastName(updatedUser.getLastName());
            if(updatedUser.getPhotoURL() != null) user.setPhotoURL(updatedUser.getPhotoURL());
            user.setDeleted(updatedUser.isDeleted());
            return userDao.updateUser(user);
        }
      return false;
    }
    public boolean updateUserByUsername(String token, String userToChangeUsername, User updatedUser){
        UserEntity userProductOwner = getUserByToken(token);
        UserEntity userToChange = getUserByUsername(userToChangeUsername);
        if(userToChange != null){
            if(userProductOwner.getRole().equals(userRoleManager.PRODUCT_OWNER) && updatedUser != null){
                if(updatedUser.getPhoneNumber() != null)userToChange.setPhoneNumber(updatedUser.getPhoneNumber());
                if(updatedUser.getEmail() != null) userToChange.setEmail(updatedUser.getEmail());
                if(updatedUser.getFirstName() != null) userToChange.setFirstName(updatedUser.getFirstName());
                if(updatedUser.getLastName() != null) userToChange.setLastName(updatedUser.getLastName());
                if(updatedUser.getPhotoURL() != null) userToChange.setPhotoURL(updatedUser.getPhotoURL());
                if(updatedUser.getRole() != null) userToChange.setRole(updatedUser.getRole());
                userToChange.setDeleted(updatedUser.isDeleted());
                if(updatedUser.isDeleted())userToChange.setToken(null);
                return userDao.updateUser(userToChange);
            }
        }
        return false;
    }
    public boolean oldPasswordConfirmation(String token, String oldPassword, String newPassword){
        UserEntity user = getUserByToken(token);
        if(user != null){
            if(user.getPassword().equals(oldPassword) && !user.getPassword().equals(newPassword)){
                return true;
            }
        }
        return false;
    }

    public boolean updatePassWord(String token, String newPassword){
        UserEntity user = getUserByToken(token);
        if(user != null){
            user.setPassword(newPassword);
            return userDao.updateUser(user);
        }
        return false;
    }

    public boolean deleteUserTemporarily(String username){
        UserEntity userEntity = userDao.findUserByUsername(username);
        if(!userEntity.getDeleted()) {
            userEntity.setDeleted(true);
            userDao.merge(userEntity);
            return true;
        }
        else return false;
    }

    public void transferTasks(String username){
        UserEntity userEntity=getUserByUsername(username);
        UserEntity admin=getUserByUsername("deletedTasks");
        ArrayList<TaskEntity> tasks=taskDao.getTasksByUser(userEntity);
        for(TaskEntity task:tasks){
            task.setUser(admin);
            taskDao.merge(task);
        }
    }

    public void transferCategories(String username){
        UserEntity userEntity=getUserByUsername(username);
        UserEntity admin=getUserByUsername("deletedTasks");
        ArrayList<CategoryEntity> categoryEntities=categoryDao.getCategoriesByUser(userEntity);
        for(CategoryEntity category:categoryEntities){
            category.setAuthor(admin);
            categoryDao.merge(category);
        }
    }

    public UserInfoCard convertUserEntityToUserInfoCard(UserEntity userEntity){
        return new UserInfoCard(userEntity.getUsername(),
                userEntity.getFirstName(),
                userEntity.getPhotoURL(),
                userEntity.getDeleted(),
                userEntity.getRole());
    }
    public List<UserInfoCard> getAllUsersInfo(){
        List<UserEntity> userEntities = userDao.findAllUsers();
        List<UserInfoCard> users = new ArrayList<>();
        if(userEntities != null){
            for(UserEntity userEntity : userEntities){
                users.add(convertUserEntityToUserInfoCard(userEntity));
            }
        }
        return users;
    }
    public boolean deleteUserPermanetely(String username){

        return userDao.deleteUser(username);
    }
    public void logout(String token){
        UserEntity user = getUserByToken(token);
        System.out.println(user);
        if(user != null){
            user.setToken(null);
            userDao.updateUser(user);
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}