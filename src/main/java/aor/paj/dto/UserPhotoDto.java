package aor.paj.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserPhotoDto {

    @XmlElement
    private String firstName;
    @XmlElement
    private String photoURL;
    @XmlElement
    private boolean deleted;
    @XmlElement
    private boolean confirmed;


    public UserPhotoDto(String firstName, String photoURL, boolean deleted, boolean confirmed) {
        this.firstName = firstName;
        this.photoURL = photoURL;
        this.deleted=deleted;
        this.confirmed=confirmed;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}
