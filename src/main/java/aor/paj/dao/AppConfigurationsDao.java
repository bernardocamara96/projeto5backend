package aor.paj.dao;

import aor.paj.entity.AppConfigurationsEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;

import java.time.LocalDateTime;

@Stateless
public class AppConfigurationsDao extends AbstractDao<AppConfigurationsEntity> {
    private static final long serialVersionUID = 1L;
    public AppConfigurationsDao() {super(AppConfigurationsEntity.class);}

    public AppConfigurationsEntity findAppConfigurationByName(String name) {
        try {
            return (AppConfigurationsEntity) em.createNamedQuery("AppConfiguration.findAppConfigurationByName").setParameter("name", name)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public long findAppConfigurationValueByName(String name) {
        try {
            return (long) em.createNamedQuery("AppConfiguration.findAppConfigurationValueByName").setParameter("name", name)
                    .getSingleResult();

        } catch (NoResultException e) {
            return 0;
        }
    }

    public boolean setConfigurationValue(long value, String name) {
        try {
            Query query = em.createNamedQuery("AppConfiguration.updateValueByName");
            query.setParameter("value", value);
            query.setParameter("name", name);

            query.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace(); // Handle the exception appropriately
            return false;
        }
    }
}
