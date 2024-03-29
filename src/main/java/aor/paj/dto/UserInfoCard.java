package aor.paj.dto;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * UserWithNoPassword is a DTO designed for safely transferring user data without exposing
 * sensitive information such as passwords. It includes user identification and contact information. This class is particularly
 * useful in scenarios where user details need to be sent over the network or displayed in
 * client applications without compromising security by including password data.
 */


@XmlRootElement
public class UserInfoCard {
    private String id;
    private String username;
    private String firstName;
    private String photoURL;
    private boolean deleted;
    private String role;

    public UserInfoCard() {
    }

    public UserInfoCard(String username, String firstName, String photoURL, boolean deleted, String role) {
        // todos os atributos são preenchidos pelo construtor a void
        this.username = username;
        this.firstName = firstName;
        this.photoURL = photoURL;
        this.deleted=deleted;
        this.role=role;
    }
    @XmlElement
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    @XmlElement
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    @XmlElement
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
    @XmlElement
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "UserInfoCard{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", photoURL='" + photoURL + '\'' +
                ", deleted=" + deleted +
                '}';
    }
}