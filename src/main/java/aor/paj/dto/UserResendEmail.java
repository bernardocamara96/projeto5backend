package aor.paj.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
@XmlRootElement
public class UserResendEmail {

    @XmlElement
    private String firstName;
    @XmlElement
    private String email;



    public UserResendEmail(String username, String email) {
        this.firstName = username;
        this.email = email;
    }

    public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
