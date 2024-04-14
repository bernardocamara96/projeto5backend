package aor.paj.entity;

import jakarta.ejb.Local;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
@Entity
@Table(name="user")
@NamedQuery(name = "User.findUserByUsername", query = "SELECT u FROM UserEntity u WHERE u.username = :username")
@NamedQuery(name = "User.findUserByEmail", query = "SELECT u FROM UserEntity u WHERE u.email = :email")
@NamedQuery(name = "User.findUserByToken", query = "SELECT DISTINCT u FROM UserEntity u WHERE u.token = :token")
@NamedQuery(name = "User.findUserByAuxiliarToken", query = "SELECT DISTINCT u FROM UserEntity u WHERE u.auxiliarToken = :auxiliarToken")
@NamedQuery(name = "User.findAllUsers", query = "SELECT u FROM UserEntity u")
@NamedQuery(name= "User.deleteUserById", query="DELETE FROM UserEntity t WHERE t.username = :username")
@NamedQuery(name = "User.countConfirmedUsers", query = "SELECT COUNT(u) FROM UserEntity u WHERE u.confirmed=true AND u.username NOT IN :excludedUsernames")
@NamedQuery(name = "User.countNotConfirmedUsers", query = "SELECT COUNT(u) FROM UserEntity u WHERE u.confirmed=false")
@NamedQuery(name = "User.findAllRegisterDates", query = "SELECT u.registerDate FROM UserEntity u WHERE u.username NOT IN :excludedUsernames")
@NamedQuery(name = "User.findRegisterDateByUsername", query = "SELECT u.registerDate FROM UserEntity u WHERE u.username=:username")

public class UserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="username", nullable=false, unique = true, updatable = false)
    private String username;
    @Column(name="password", nullable=true, unique = false, updatable = true)
    private String password;
    @Column(name="email", nullable=false, unique = true, updatable = true)
    private String email;
    @Column(name="firstname", nullable=false, unique = false, updatable = true)
    private String firstName;
    @Column(name="lastname", nullable=false, unique = false, updatable = true)
    private String lastName;
    @Column(name="phonenumber", nullable=false, unique = false, updatable = true)
    private String phoneNumber;
    @Column(name="photourl", nullable=false, unique = false, updatable = true)
    private String photoURL;
    @Column(name="token", nullable=true, unique = true, updatable = true)
    private String token;
    @Column(name="auxiliartoken", nullable=true, unique = true, updatable = true)
    private String auxiliarToken;
    @Column(name="role", nullable=true, unique = false, updatable = true)
    private String role;
    @Column(name="confirmed", nullable = false,unique = false,updatable = true)
    private boolean confirmed;
    @Column(name="deleted", nullable = false,unique = false,updatable = true)
    private boolean deleted;
    @Column(name="register_date", nullable = false,unique = false,updatable = false)
    private LocalDateTime registerDate;

    @OneToMany(mappedBy = "user")
    private Set<TaskEntity> tasks;

    @OneToMany(mappedBy = "author")
    private Set<CategoryEntity>  categories;

    //default empty constructor
    public UserEntity() {}

    public UserEntity(String username, String password, String email, String firstName, String lastName,
                      String phoneNumber, String photoURL, String token, String role, boolean deleted, boolean confirmed) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.photoURL = photoURL;
        this.token = token;
        this.role = role;
        this.deleted = deleted;
        this.confirmed=confirmed;
        this.registerDate=LocalDateTime.now();
    }

    public String getAuxiliarToken() {
        return auxiliarToken;
    }

    public void setAuxiliarToken(String auxiliarToken) {
        this.auxiliarToken = auxiliarToken;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public void setTasks(Set<TaskEntity> tasks) {
        this.tasks = tasks;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public Set<TaskEntity> getTasks() {
        return tasks;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(LocalDateTime registerDate) {
        this.registerDate = registerDate;
    }

    public Set<CategoryEntity> getCategories() {
        return categories;
    }

    public void setCategories(Set<CategoryEntity> categories) {
        this.categories = categories;
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", photoURL='" + photoURL + '\'' +
//                ", token='" + token + '\'' +
                '}';
    }


}
