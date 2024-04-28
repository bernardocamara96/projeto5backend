package aor.paj.bean;

import aor.paj.dao.TaskDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.TaskUpdate;
import aor.paj.dto.User;
import aor.paj.dto.UserWithNoPassword;
import aor.paj.emailsender.EmailSender;
import aor.paj.entity.UserEntity;
import aor.paj.service.validator.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserBeanTest {

    @InjectMocks
    private UserBean userBean;

    @Mock
    private UserDao userDao;

    @Mock
    private TaskDao taskDao;

    @Mock
    private UserEntity testUser;
    @Mock
    private User testUserUP;
    @Mock
    private UserValidator userValidator;
    @Mock
    EmailSender emailSender;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new UserEntity("testUser", "securePassword123", "testUser@example.com",
                "Test", "User", "123-456-7890", "https://example.com/photo.jpg",
                "uniqueToken123456", "USER", false,true);

        when (userDao.updateUser(any(UserEntity.class))).thenReturn(true);
        when (userDao.deleteUser(anyString())).thenReturn(true);
        when(userDao.findUserByUsername(anyString())).thenReturn(testUser);
        when(userDao.findUserByEmail(anyString())).thenReturn(null);
        when(userDao.findUserByToken(anyString())).thenReturn(testUser);
        when(userDao.findUserByUsername(anyString())).thenReturn(testUser);
        when(userDao.findUserByEmail(anyString())).thenReturn(null);
        when(userDao.findUserByUsername(anyString())).thenReturn(testUser);
        when(userDao.findUserByEmail(anyString())).thenReturn(null);
        when(userDao.findUserByToken(anyString())).thenReturn(testUser);
        when(userDao.findUserByUsername(anyString())).thenReturn(testUser);
        when(userDao.findUserByEmail(anyString())).thenReturn(null);
    }

    @Test
    void testRegisterUser_Success() {
        User newUser = new User();
        newUser.setUsername("newUser");
        newUser.setPassword("newPassword");
        newUser.setEmail("newUser@example.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setPhoneNumber("987-654-3210");
        newUser.setPhotoURL("https://example.com/newphoto.jpg");
        newUser.setRole("DEVELOPER");

        userBean.register(newUser);

        verify(userDao, times(1)).persist(any(UserEntity.class));
    }

    @Test
    void testUpdateUser_Success() {
        testUserUP = new User();
        testUserUP.setEmail("testUser@example.com");
        testUserUP.setFirstName("Updated");
        testUserUP.setLastName("User");
        testUserUP.setPhoneNumber("1234567890");
        testUserUP.setPhotoURL("https://example.com/updatedphoto.jpg");
        testUserUP.setDeleted(false);

        boolean result = userBean.updateUser("uniqueToken123456", testUserUP);

        assertTrue(result);
        verify(userDao, times(1)).updateUser(any(UserEntity.class));
    }

    @Test
    void testDeleteUserTemporarily_Success() {
        boolean result = userBean.deleteUserTemporarily("testUser");
        assertTrue(result);
        verify(userDao, times(1)).merge(any(UserEntity.class));
    }

    @Test
    void testDeleteUserPermanently_Success() {
        boolean result = userBean.deleteUserPermanetely("testUser");

        assertTrue(result);
        verify(userDao, times(1)).deleteUser("testUser");
    }
    @Test
    void testRegisterUser_Failure() {

        doThrow(RuntimeException.class).when(userDao).persist(any(UserEntity.class));

        User newUser = new User();
        newUser.setUsername("existingUser"); // Supondo que este usuário já existe
        newUser.setPassword("password");
        newUser.setEmail("existingUser@example.com");
        newUser.setFirstName("Existing");
        newUser.setLastName("User");
        newUser.setPhoneNumber("987-654-3210");
        newUser.setPhotoURL("https://example.com/existingphoto.jpg");
        newUser.setRole("DEVELOPER");

        Exception exception = assertThrows(RuntimeException.class, () -> userBean.register(newUser));

        assertNotNull(exception);
    }
    @Test
    void testUpdateUser_Failure() {

        when(userDao.findUserByToken(anyString())).thenReturn(null);

        User userToUpdate = new User();
        userToUpdate.setEmail("nonexistentUser@example.com");

        boolean result = userBean.updateUser("nonexistentToken", userToUpdate);

        assertFalse(result);
    }
    @Test
    void testDeleteUserTemporarily_Failure() {

        testUser.setDeleted(true);
        when(userDao.findUserByUsername("testUser")).thenReturn(testUser);

        boolean result = userBean.deleteUserTemporarily("testUser");

        assertFalse(result);
    }

    @Test
    void testDeleteUserPermanently_Failure() {

        when(userDao.deleteUser("nonexistentUser")).thenReturn(false);

        boolean result = userBean.deleteUserPermanetely("nonexistentUser");

        assertFalse(result);
    }


}
