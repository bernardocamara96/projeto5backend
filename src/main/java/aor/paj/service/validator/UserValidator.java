package aor.paj.service.validator;

import aor.paj.dto.User;
import aor.paj.service.status.userRoleManager;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * UserValidator provides a series of validation methods for user-related data, ensuring each piece of information
 * meets specific criteria before proceeding with user registration or updates. It validates usernames, emails,
 * phone numbers, names, photo URLs, and passwords for both new registrations and user profile edits.
 */

@ApplicationScoped
public class UserValidator {

    public UserValidator() {

    }

    /**Validation*/
    private boolean validateUsername(String username) {
        boolean isValid = username != null && username.length() >= 2 && username.length() <= 25;

        return isValid;
    }

    private boolean validateEmail(String email) {
        boolean isValid = email != null && email.contains("@") && email.indexOf('@') < email.lastIndexOf('.')
                && email.length() >= 5 && email.length() <= 500;

        return isValid;
    }

    private boolean validatePhone(String phone) {
        if (phone == null) return false;
        // Verifica o comprimento do número de telefone limpo.
        if (phone.length() < 7 || phone.length() > 20) return false;
        // Se passou por todas as verificações, o número é válido.
        return true;
    }
    private boolean validateName(String firstName, String lastName) {
        if (firstName == null || lastName == null) return false;
        boolean isValid = firstName.length() >= 1 && firstName.length() <= 25 &&
                lastName.length() >= 1 && lastName.length() <= 25;

        return isValid;
    }

    private boolean validatePhotoURL(String photoURL) {
        boolean isValid = photoURL != null && photoURL.length() >= 3 && photoURL.length() <= 500;

            return isValid;
    }
    public boolean validatePassword(String password) {
        // Verifica se a senha não é nula e se tem pelo menos 6 caracteres
        boolean isValid = password != null && password.length() >= 4;
        return isValid;
    }
    public boolean validateRole(String role) {
        if (role == null) return false;
        return role.equals(userRoleManager.DEVELOPER) ||
                role.equals(userRoleManager.SCRUM_MASTER) ||
                role.equals(userRoleManager.PRODUCT_OWNER);
    }
    public boolean validateUserOnRegistration(User user) {
        System.out.println(validateUsername(user.getUsername()));
        System.out.println(validatePassword(user.getPassword()));
        System.out.println(validateEmail(user.getEmail()));
        System.out.println(validatePhone(user.getPhoneNumber()));
        System.out.println(validateName(user.getFirstName(), user.getLastName()));
        System.out.println( validatePhotoURL(user.getPhotoURL()));
        System.out.println(validateRole(user.getRole()));
        return validateUsername(user.getUsername()) &&
                validatePassword(user.getPassword()) &&
                validateEmail(user.getEmail()) &&
                validatePhone(user.getPhoneNumber()) &&
                validateName(user.getFirstName(), user.getLastName()) &&
                validatePhotoURL(user.getPhotoURL()) &&
                validateRole(user.getRole());
    }
    public boolean validateUserOnEdit(User user) {
        boolean isValid = true;
        if(user.getPhoneNumber() != null){
            if(!validatePhone(user.getPhoneNumber())){
                isValid = false;
            }
        }
        if(user.getFirstName() != null && user.getLastName() != null){
            if(!validateName(user.getFirstName(), user.getLastName())){
                isValid = false;
            }
        }
        if(user.getPhotoURL() != null){
            if(!validatePhotoURL(user.getPhotoURL())){
                isValid = false;
            }
        }
        if(user.getEmail() != null){
            if(!validateEmail(user.getEmail())){
                isValid = false;
            }
        }
        if(user.getRole() != null){
            if(!validateRole(user.getRole())){
                isValid = false;
            }
        }
        return isValid;
    }
}