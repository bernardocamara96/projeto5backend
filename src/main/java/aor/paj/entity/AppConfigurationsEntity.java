package aor.paj.entity;


import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table (name="configurations")
@NamedQuery(name="AppConfiguration.findAppConfigurationByName", query="SELECT c FROM AppConfigurationsEntity c WHERE c.name=:name")
@NamedQuery(name="AppConfiguration.findAppConfigurationValueByName", query="SELECT c.value FROM AppConfigurationsEntity c WHERE c.name=:name")
@NamedQuery(name = "AppConfiguration.updateValueByName", query = "UPDATE AppConfigurationsEntity c SET c.value = :value WHERE c.name = :name")

public class AppConfigurationsEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="name", nullable = false,unique = true,updatable = true)
    private String name;

    @Column(name="value", nullable = true,unique = false,updatable = true)
    private long value;


    public AppConfigurationsEntity() {}
    public AppConfigurationsEntity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
