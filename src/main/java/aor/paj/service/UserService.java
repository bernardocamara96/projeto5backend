package aor.paj.service;

import aor.paj.bean.AppConfigurationsBean;
import aor.paj.bean.PermissionBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.*;
import aor.paj.entity.UserEntity;
import aor.paj.service.status.Function;
import aor.paj.service.validator.UserValidator;
import aor.paj.websocket.DashboardWebSocket;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;


@Path("/users")

public class UserService {
    @EJB
    UserBean userBean;
    @Inject
    UserValidator userValidator;
    @EJB
    PermissionBean permissionBean;
    @EJB
    AppConfigurationsBean appConfigurationsBean;
    @Inject
    DashboardWebSocket dashboardWebSocket;

    /**
     * This endpoint is responsible for adding a new user to the system. It accepts JSON-formatted requests
     * containing user data and processes the request accordingly.
     * If the provided user data fails validation, it returns a status code of 400 (Bad Request) with the message
     * "Invalid Data".
     * If a user with the same username or email already exists in the system, it returns a status code of 409
     * (Conflict) with the message "Username or Email already Exists".
     * If the user is successfully added to the system, it returns a status code of 200 (OK) with the message
     * "A new user was created".
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addUser(User user) {
        if (!userValidator.validateUserOnRegistration(user)) {
            return Response.status(422).entity("Invalid Data").build();
        }
        if (userBean.checkIfUserExists(user)) {
            return Response.status(409).entity("Username already Exists").build();
        }
        if (userBean.checkIfemailExists(user)) {
            return Response.status(409).entity("Email already Exists").build();
        }
        String token = userBean.register(user);
        if (token!=null) {
            try {
                dashboardWebSocket.send();
            } catch (IOException e) {}
            return Response.status(200).entity("{\n" +
                    "  \"token\": \""+token+"\"\n" +
                    "}").build();
        } else {
            return Response.status(500).entity("An error occurred while registering the user").build();
        }
    }


    /**
     * This endpoint is responsible for user authentication. It accepts JSON-formatted requests containing
     * user credentials (username and password) as headers. It returns appropriate responses indicating the
     * success or failure of the login attempt.
     * Successful login returns a status code of 200, failed login returns 401, and missing username or password
     * returns 422.
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(LoginDto user) {
        String token = userBean.login(user);
        String auxiliarToken= userBean.getAuxiliarToken(user);
        boolean confirmed = userBean.getConfirmed(user);

        if (token != null) {
            appConfigurationsBean.setLastActivityDate(token);
            return Response.status(200).entity("{\n" +
                    "  \"token\": \"" + token + "\",\n" +
                    "  \"auxiliarToken\": \"" + auxiliarToken + "\",\n" +
                    "  \"confirmed\": \"" + confirmed + "\"\n" +
                    "}").build();
        }
       else return Response.status(401).entity("Login Failed").build();
    }



    /**
     * Retrieves the photo URL and the first name associated with the provided username.
     * If the username and password are not provided in the request headers, returns a status code 401 (Unauthorized)
     * with the error message "User not logged in".
     * If the provided credentials are invalid, returns a status code 403 (Forbidden) with the error message "Access denied".
     * If the photo URL and first name are found for the given username, returns a status code 200 (OK) with the photo URL and first name in JSON format.
     * If no photo URL or first name is found for the given username, returns a status code 404 (Not Found) with the error message "No photo or name found".
     */
    @GET
    @Path("/photoandname")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPhoto(@HeaderParam("token")String token) {
        if (userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                String photoUrl = userBean.getPhotoURLByUsername(token);
                String name = userBean.getFirstNameByToken(token);
                String role = userBean.getRoleByToken(token);
                if (photoUrl != null) {
                    appConfigurationsBean.setLastActivityDate(token);
                    return Response.status(200).entity("{\"photoUrl\":\"" + photoUrl + "\", \"name\":\"" + name + "\", \"role\":\"" + role + "\"}").build();}
                return Response.status(404).entity("No photo found").build();
            } else return Response.status(401).entity("Session has expired").build();
        } else return Response.status(403).entity("Access denied").build();
    }

    @GET
    @Path("/photoandname/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPhoto(@HeaderParam("token")String token, @PathParam("username")String username) {
        if (userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                    UserPhotoDto userPhotoDto=userBean.getUserPhotoDtoByUsername(username);
                    appConfigurationsBean.setLastActivityDate(token);
                    return Response.status(200).entity(userPhotoDto).build();
            } else return Response.status(401).entity("Session has expired").build();
        } else return Response.status(403).entity("Access denied").build();
    }
    /**
     * Retrieves user information for the given username.
     * If the username or password is missing in the request headers, returns a status code 401 (Unauthorized)
     * with the error message "User not logged in".
     * If the provided credentials are invalid, returns a status code 403 (Forbidden) with the error message "Access denied".
     * If the user information is successfully retrieved, returns a status code 200 (OK) with the user information
     * (without the password) in JSON format.
     */
    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    public Response userInfo(@HeaderParam("username") String username, @HeaderParam("token")String token) {
        if(userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                if (userBean.checkIfUserExists(username)) {
                    UserEntity userEntity = userBean.getUserByUsername(username);
                    UserWithNoPassword userWithoutPassword = userBean.convertUserEntityToUserWithNoPassword(userEntity);
                    appConfigurationsBean.setLastActivityDate(token);
                    return Response.status(200).entity(userWithoutPassword).build();
                } else return Response.status(403).entity("Access denied").build();
            } else return Response.status(401).entity("Session has expired").build();
        }else return Response.status(403).entity("Access denied").build();
    }

    /**
     * Retrieves detailed information for a user based on a provided username. It checks if the request
     * is authenticated and authorized to access the information. This endpoint is useful for obtaining
     * user details without exposing sensitive information like passwords.
     */

    @GET
    @Path("/info/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response userInfoByusername(@HeaderParam("token") String token , @PathParam("username") String username) {
        if(username == null)
            return Response.status(400).entity("Invalid Data").build();
        else if (userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                if (permissionBean.getPermission(token, Function.GET_OTHER_USER_INFO)) {
                    UserEntity userEntity = userBean.getUserByUsername(username);
                    UserWithNoPassword userWithoutPassword = userBean.convertUserEntityToUserWithNoPassword(userEntity);
                    appConfigurationsBean.setLastActivityDate(token);
                    return Response.status(200).entity(userWithoutPassword).build();
                } else return Response.status(403).entity("User permissions violated").build();

            }else return Response.status(401).entity("Session has expired").build();
        } else return Response.status(401).entity("Login Failed").build();
    }

    /**
     * Provides a list of all registered users in the system. It requires authentication and checks
     * for the necessary permissions before proceeding. This endpoint is typically used by administrators
     * or users with specific roles that allow viewing all user accounts.
     */
     @GET
     @Path("/")
     @Produces(MediaType.APPLICATION_JSON)
     public Response getAllUsers(@HeaderParam("token") String token) {
         if (userBean.tokenValidator(token)) {
             if(appConfigurationsBean.validateTimeout(token)) {
                 appConfigurationsBean.setLastActivityDate(token);
                 return Response.status(200).entity(userBean.getAllUsersInfo()).build();
             }else return Response.status(401).entity("Session has expired").build();
         }return Response.status(401).entity("Access denied").build();
     }

    /**
     * Fetches the role of the user making the request. This endpoint is used to determine the
     * user's permissions within the application based on their role. It requires a valid authentication
     * token to identify the user.
     */
    @GET
    @Path("/role")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRole(@HeaderParam("token")String token){
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)){
                String role=userBean.getRoleByToken(token);
                appConfigurationsBean.setLastActivityDate(token);
                return Response.status(200).entity("{\"role\":\"" + role + "\"}").build();
            }else return Response.status(401).entity("Session has expired").build();
        } else return Response.status(403).entity("User permissions violated").build();
    }
    /**
     * Allows an authenticated user to update their own data. It checks for valid authentication and
     * proper permissions before allowing the update. The method ensures that the user can only update
     * their own information and not that of others unless specifically authorized.
     */
    @PATCH
    @Path("/data")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editUserData(User updatedUser, @HeaderParam("token") String token) {
        if (userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                if (permissionBean.getPermission(token, Function.EDIT_OWN_USER_INFO)) {
                    if (userValidator.validateUserOnEdit(updatedUser) && updatedUser.getUsername() == null) {
                        if (!userBean.checkIfUserExists(updatedUser)) {
                            boolean updateResult = userBean.updateUser(token, updatedUser);
                            if (updateResult){
                                appConfigurationsBean.setLastActivityDate(token);
                                return Response.status(200).entity("User data updated successfully").build();}
                            else
                                return Response.status(500).entity("An error occurred while updating user data").build();
                        }
                        return Response.status(409).entity("Username or Email already Exists").build();
                    }
                    return Response.status(400).entity("Invalid Data").build();
                }
                return Response.status(403).entity("Access denied").build();
            }else return Response.status(401).entity("Session has expired").build();
        }return Response.status(401).entity("Login Failed").build();
    }


    @PATCH
    @Path("/confirmed/{confirmed}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response confirmUser(@PathParam("confirmed") boolean confirmed, @HeaderParam("pass") String pass,@HeaderParam("token") String token) {
        if (userBean.auxiliarTokenValidator(token)) {
            boolean updatePass = userBean.updatePassWord(token, pass);
            boolean updateResult = userBean.updateUserConfirmed(token,confirmed);
            if (updateResult && updatePass){
                try {
                    dashboardWebSocket.send();
                } catch (IOException e) {}
                return Response.status(200).entity("User data updated successfully").build();
            }
            else return Response.status(500).entity("An error occurred while updating user data").build();
        }return Response.status(401).entity("Access denied").build();
    }

    @POST
    @Path("/newpassemail")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response newPassEmail(@HeaderParam("email") String email) {
        if (userValidator.validateEmail(email)) {
            if(userBean.checkIfemailExists(email)) {
                if (userBean.sendResetPassMail(email)) {
                    return Response.status(200).entity("Mail sent successfully").build();
                } else return Response.status(400).entity("Error sending mail").build();
            }else return Response.status(404).entity("Email not found").build();
        }else return Response.status(406).entity("Email not valid").build();
    }

    @GET
    @Path("/auxiliartokenvalidator")
    @Produces(MediaType.APPLICATION_JSON)
    public Response auxiliarTokenValidator(@HeaderParam("token") String token) {
        if (userBean.auxiliarTokenValidator(token)) {
            return Response.status(200).entity("Token was validated").build();
        }else return Response.status(404).entity("Token not valid").build();
    }


    @POST
    @Path("/recoverpassword")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response recoverPassword(@HeaderParam("newPass")String newPass, @HeaderParam("token") String token) {
        if (userBean.auxiliarTokenValidator(token)) {
            if (userValidator.validatePassword(newPass)) {
                if(userBean.isConfirmed(token)) {
                    boolean updateResult = userBean.updatePassWord(token, newPass);
                    if (updateResult) {
                        userBean.clearToken(token);
                        return Response.status(200).entity("User password updated successfully").build();
                    } else return Response.status(500).entity("An error occurred while updating user password").build();
                } else return Response.status(404).entity("User still not confirmed").build();
            } else return Response.status(400).entity("Invalid Data").build();
        }else return Response.status(401).entity("Access denied").build();
    }

    @POST
    @Path("/resendemail")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response resendEmail(@HeaderParam("token") String token) {
        if(userBean.auxiliarTokenValidator(token)){
            UserResendEmail user=userBean.getUserResendEmailByToken(token);
            String newToken=userBean.setNewToken(token);
            if(userBean.sendNewEmail(user.getFirstName(),user.getEmail(),newToken)){
                return Response.status(200).entity("Email was sent").build();
            }
            else return Response.status(401).entity("There was an error sending the email").build();
        }
        else return Response.status(401).entity("Access denied").build();
    }

    /**
     * Allows an administrator to edit another user's data, given a specific username. This endpoint
     * ensures that only users with the appropriate permissions can make changes to other user accounts.
     * It performs checks to ensure that the email and username remain unique and not already in use.
     */

     @PATCH
    @Path("/otheruser")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response adminEditUserData(User updatedUser, @HeaderParam("token") String token,
                                      @HeaderParam("userToChangeUsername") String username) {
        if (userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                if (permissionBean.getPermission(token, Function.EDIT_OTHER_USER_INFO)) {
                    if (userValidator.validateUserOnEdit(updatedUser) && updatedUser.getUsername() == null) {
                        if (userBean.checkIfUserExists(username)) {
                            if (!userBean.checkIfemailExists(updatedUser)) {
                                boolean updateResult = userBean.updateUserByUsername(token, username, updatedUser);
                                if (updateResult){
                                    appConfigurationsBean.setLastActivityDate(token);
                                    return Response.status(200).entity("User data updated successfully").build();}
                                else
                                    return Response.status(400).entity("An error occurred while updating user data").build();
                            }
                            return Response.status(409).entity("Username or Email already Exists").build();
                        }
                        return Response.status(409).entity("Username do not Exists").build();
                    }
                    return Response.status(400).entity("Invalid Data").build();
                }
                return Response.status(403).entity("Access denied").build();

            }else return Response.status(401).entity("Session has expired").build();
        }return Response.status(401).entity("Login Failed").build();
    }


    /**
     * Enables a user to update their password. It requires the old password for verification
     * and checks if the new password meets the system's security requirements. This endpoint
     * is crucial for maintaining user account security.
     */
    @POST
    @Path("/password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editUserPassword(UserNewPassword updatedPassword, @HeaderParam("token") String token) {
        if (userBean.tokenValidator(token) && userBean.oldPasswordConfirmation(token, updatedPassword.getPassword(),
                updatedPassword.getNewPassword())) {
            if(appConfigurationsBean.validateTimeout(token)) {
                if (userValidator.validatePassword(updatedPassword.getNewPassword())) {
                    boolean updateResult = userBean.updatePassWord(token, updatedPassword.getNewPassword());
                    if (updateResult){
                        appConfigurationsBean.setLastActivityDate(token);
                        return Response.status(200).entity("User password updated successfully").build();}
                    else return Response.status(500).entity("An error occurred while updating user password").build();
                }
                return Response.status(400).entity("Invalid Data").build();
            }else return Response.status(401).entity("Session has expired").build();
        }return Response.status(401).entity("Login Failed, Passwords do not match or New password must be different from the old password").build();
    }


    /**
     * Permanently deletes a user from the system based on the specified username. This operation is irreversible
     * and involves transferring any tasks or categories associated with the user to a default state before
     * deletion to ensure data consistency. This endpoint requires authentication and specific permissions,
     * typically reserved for administrators, to execute this action.
     */
    @DELETE
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteTaskPermanently(@HeaderParam("token")String token, @HeaderParam("userToDeleteUsername")String username){
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                if (permissionBean.getPermission(token, Function.PERMANENTLY_USER_DELET)) {
                    if (userBean.checkIfUserExists(username)) {
                        if (!username.equals("admin") && !username.equals("deletedTasks")) {
                            userBean.transferTasks(username);
                            userBean.transferCategories(username);
                            boolean successfullyDeleted = userBean.deleteUserPermanetely(username);
                            if (successfullyDeleted){
                                appConfigurationsBean.setLastActivityDate(token);
                                try {
                                    dashboardWebSocket.send();
                                } catch (IOException e) {}
                                return Response.status(200).entity("This user permanently deleted ").build();}
                            else return Response.status(400).entity("User not deleted").build();
                        } else return Response.status(400).entity("Admin can't be deleted.").build();
                    } else return Response.status(400).entity("User with this id not found").build();
                } else return Response.status(403).entity("User permissions violated").build();
            }else return Response.status(401).entity("Session has expired").build();
        } else return Response.status(403).entity("User permissions violated").build();
    }



    /**
     * This endpoint makes logging out a user. Since this example does not
     * manage user sessions or authentication tokens explicitly, the endpoint simply returns
     * a response indicating that the user has been logged out successfully.
     *  */
    @POST
    @Path("/logout")
    public Response logout(@HeaderParam("token")String token) {
        if (token == null) {
            return Response.status(422).entity("Missing token").build();
        }
        userBean.logout(token);
        return Response.status(200).entity("User logged out successfully").build();
    }

    @GET
    @Path("/configuration/{name}")
    public Response getConfigurationValue(@HeaderParam("token")String token, @PathParam("name")String name) {
      if (userBean.tokenValidator(token)) {
          if(appConfigurationsBean.validateTimeout(token)) {
              return Response.status(200).entity(appConfigurationsBean.getConfigurationValueByName(name)).build();
          }else return Response.status(401).entity("Session has expired").build();
      }else return Response.status(403).entity("Access denied").build();
    }

    @PATCH
    @Path("/configuration/{name}/{value}")
    public Response getConfigurationValue(@HeaderParam("token")String token, @PathParam("name")String name, @PathParam("value")String value) {
      long valueLong=0;
      try {
          valueLong = Long.parseLong(value);
      }catch (NumberFormatException e){
           return Response.status(400).entity("Invalid data").build();
      }

        if (userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                if(appConfigurationsBean.setConfigurationValue(valueLong,name)) {
                    return Response.status(200).entity("Configurations updated successfully").build();
                }else  return Response.status(400).entity("Invalid data").build();
            }else return Response.status(401).entity("Session has expired").build();
        }else return Response.status(403).entity("Access denied").build();
    }
}