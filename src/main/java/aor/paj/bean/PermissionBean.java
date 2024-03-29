package aor.paj.bean;

import aor.paj.dao.TaskDao;
import aor.paj.dao.UserDao;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import aor.paj.service.status.Function;
import aor.paj.service.status.userRoleManager;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Stateless
public class PermissionBean {
    @EJB
    UserDao userDao;
    @EJB
    TaskDao taskDao;
    private final HashMap<String, Set<Function>> rolePermissions = new HashMap<>();

    public PermissionBean() {

        //Product Owner
        HashSet<Function> productOwnerPermissions = new HashSet<>();
        productOwnerPermissions.add(Function.PERMANENTLY_USER_DELET);
        productOwnerPermissions.add(Function.GET_OTHER_USER_INFO);
        productOwnerPermissions.add(Function.EDIT_OWN_USER_INFO);
        productOwnerPermissions.add(Function.EDIT_OTHER_USER_INFO);
        productOwnerPermissions.add(Function.EDIT_OR_DELETE_OTHER_USER_TASK);
        productOwnerPermissions.add(Function.GET_ALL_TASKS);
        productOwnerPermissions.add(Function.GET_ALL_TASKS_BY_USER);
        productOwnerPermissions.add(Function.GET_ALL_TASKS_BY_CATEGORY);
        productOwnerPermissions.add(Function.GET_ALL_TASKS_BY_CATEGORY_AND_USER);
        productOwnerPermissions.add(Function.RECYCLY_TASK_BY_ID);
        productOwnerPermissions.add(Function.DELETE_ALL_TASKS_BY_USER_TEMPORARILY);
        productOwnerPermissions.add(Function.DELETE_TASK_PERMANENTLY);
        productOwnerPermissions.add(Function.GET_ALL_TASKS_DELETED);

        //Scrum Master
        HashSet<Function> scrumMasterPermissions = new HashSet<>();
        scrumMasterPermissions.add(Function.GET_OTHER_USER_INFO);
        scrumMasterPermissions.add(Function.EDIT_OWN_USER_INFO);
        scrumMasterPermissions.add(Function.EDIT_OR_DELETE_OTHER_USER_TASK);
        scrumMasterPermissions.add(Function.GET_ALL_TASKS_BY_USER);
        scrumMasterPermissions.add(Function.GET_ALL_TASKS_BY_CATEGORY);
        scrumMasterPermissions.add(Function.GET_ALL_TASKS_BY_CATEGORY_AND_USER);
        scrumMasterPermissions.add(Function.RECYCLY_TASK_BY_ID);
        scrumMasterPermissions.add(Function.GET_ALL_TASKS_DELETED);

        //Developer
        HashSet<Function> developerPermissions = new HashSet<>();
        developerPermissions.add(Function.EDIT_OWN_USER_INFO);

        rolePermissions.put(userRoleManager.PRODUCT_OWNER, productOwnerPermissions);
        rolePermissions.put(userRoleManager.SCRUM_MASTER, scrumMasterPermissions);
        rolePermissions.put(userRoleManager.DEVELOPER, developerPermissions);
    }

    public String getRoleByToken(String token){
        UserEntity user = userDao.findUserByToken(token);
        return user.getRole();
    }
    public boolean getPermission(String token, Function function){
        String role = getRoleByToken(token);
        Set<Function> permissions = rolePermissions.get(role);
        if (permissions != null) {
            return permissions.contains(function);
        }
        return false;
    }
    public boolean getPermissionByTaskID(String token, int task_id){
        UserEntity userEntity= userDao.findUserByToken(token);
        TaskEntity taskEntity= taskDao.findTaskById(task_id);
        if(userEntity!=null && taskEntity!=null) {
            if (userEntity.getUsername().equals(taskEntity.getUser().getUsername()) ||
                    getPermission(token, Function.EDIT_OR_DELETE_OTHER_USER_TASK)) {
                return true;
            }return false;
        }return false;
    }
}
