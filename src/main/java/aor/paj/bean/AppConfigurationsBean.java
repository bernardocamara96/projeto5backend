package aor.paj.bean;


import aor.paj.dao.AppConfigurationsDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.User;
import aor.paj.entity.AppConfigurationsEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.time.Duration;
import java.time.LocalDateTime;

@Stateless
public class AppConfigurationsBean {

    @Inject
    AppConfigurationsDao appConfigurationsDao;
    @Inject
    UserDao userDao;

/** Creates default application configurations if they do not exist already. **/
    public void createDefaultAppConfigurations(){
        AppConfigurationsEntity timeoutConfig= appConfigurationsDao.findAppConfigurationByName("session_timeout");
        if (timeoutConfig == null){
            appConfigurationsDao.persist(new AppConfigurationsEntity("session_timeout"));
        }
    }

    public boolean validateTimeout(String token) {
        long sessionTimeout = appConfigurationsDao.findAppConfigurationValueByName("session_timeout");
        if (sessionTimeout > 0) {
            UserEntity user=userDao.findUserByToken(token);

            if(user.getUsername().equals("admin")) return true;

            LocalDateTime lastActivityDate = user.getLastActivityDate();

            Duration duration = Duration.between(lastActivityDate,LocalDateTime.now());
            long durationMinutes = duration.toMinutes();

            if (durationMinutes >= sessionTimeout) {
                user.setToken(null);
                return false;
            }
            else {
                userDao.setLastActivityDate(LocalDateTime.now(),token);
                return true;}
        }
        else {
            userDao.setLastActivityDate(LocalDateTime.now(),token);
            return true;}
    }

    public boolean setLastActivityDate(String token){
        return userDao.setLastActivityDate(LocalDateTime.now(),token);
    }

    public boolean clearLastActivityDate(String token){
        return userDao.setLastActivityDate(null,token);
    }

    public long getConfigurationValueByName(String name){
        return appConfigurationsDao.findAppConfigurationValueByName(name);
    }

    public boolean setConfigurationValue(long value, String name){
        return appConfigurationsDao.setConfigurationValue(value,name);
    }
}
