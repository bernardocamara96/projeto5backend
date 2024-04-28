package aor.paj.bean;

import aor.paj.dao.AppConfigurationsDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.User;
import aor.paj.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppConfigurationsBeanTest {
    @InjectMocks
    private AppConfigurationsBean appConfigurationsBean;

    @Mock
    private AppConfigurationsDao appConfigurationsDao;

    @Mock
    private UserDao userDao;
    @Mock
    private UserEntity mockedUserEntity;

    @BeforeEach
    void setUp() {
        LocalDateTime lastActivityDate = LocalDateTime.now().minus(31, ChronoUnit.MINUTES);
        MockitoAnnotations.openMocks(this);
        UserEntity nonAdminUser = new UserEntity("nonAdmin", "password", "nonadmin@example.com", "Non", "Admin", "123123123","https://icons.veryicon.com/png/o/miscellaneous/yuanql/icon-admin.png","userToken","developer",false,true);
        nonAdminUser.setLastActivityDate(lastActivityDate);

        when(userDao.findUserByToken("userToken")).thenReturn(nonAdminUser);
    }
    @Test
    void testValidateTimeout_UserInactive() {

        when(appConfigurationsDao.findAppConfigurationValueByName("session_timeout")).thenReturn(30L); // Assume session timeout is 30 minutes

        boolean result = appConfigurationsBean.validateTimeout("userToken");

        assertFalse(result);
    }

    @Test
    void testValidateTimeout_UserActive() {

        when(appConfigurationsDao.findAppConfigurationValueByName("session_timeout")).thenReturn(32L); // Assume session timeout is 30 minutes

        boolean result = appConfigurationsBean.validateTimeout("userToken");

        verify(userDao, times(1)).setLastActivityDate(any(LocalDateTime.class), eq("userToken"));

        assertNotNull(userDao.findUserByToken("userToken").getToken());
        assertTrue(result);
    }

}