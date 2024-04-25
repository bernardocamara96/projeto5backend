package aor.paj.service;

import aor.paj.bean.AppConfigurationsBean;
import aor.paj.bean.MessageBean;
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
import org.apache.logging.log4j.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;


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
    @EJB
    MessageBean messageBean;
    private static final Logger logger=LogManager.getLogger(UserService.class);


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
    public Response addUser(User user) throws UnknownHostException {
        if (!userValidator.validateUserOnRegistration(user)) {
            logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+" failed registration");
            return Response.status(422).entity("Invalid Data").build();
        }
        if (userBean.checkIfUserExists(user)) {
            logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+" failed registration");
            return Response.status(409).entity("Username already Exists").build();
        }
        if (userBean.checkIfemailExists(user)) {
            logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+" failed registration");
            return Response.status(409).entity("Email already Exists").build();
        }
        String token = userBean.register(user);
        if (token!=null) {
            try {
                logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+user.getUsername()+" registered");
                dashboardWebSocket.send();
            } catch (IOException e) {}
            return Response.status(200).entity("{\n" +
                    "  \"token\": \""+token+"\"\n" +
                    "}").build();
        } else {
            logger.error(InetAddress.getLocalHost().getHostAddress()+"  "+" failed registration");
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
    public Response login(LoginDto user) throws UnknownHostException {
        String token = userBean.login(user);
        String auxiliarToken= userBean.getAuxiliarToken(user);
        boolean confirmed = userBean.getConfirmed(user);

        if (token != null) {
            logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+user.getUsername()+" logged in");
            appConfigurationsBean.setLastActivityDate(token);
            return Response.status(200).entity("{\n" +
                    "  \"token\": \"" + token + "\",\n" +
                    "  \"auxiliarToken\": \"" + auxiliarToken + "\",\n" +
                    "  \"confirmed\": \"" + confirmed + "\"\n" +
                    "}").build();
        }
       else {
            logger.error(InetAddress.getLocalHost().getHostAddress()+" failed logged in");
           return Response.status(401).entity("Login Failed").build();
       }
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
    public Response getPhoto(@HeaderParam("token")String token) throws UnknownHostException {
        if (userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                String photoUrl = userBean.getPhotoURLByUsername(token);
                String name = userBean.getFirstNameByToken(token);
                String role = userBean.getRoleByToken(token);
                if (photoUrl != null) {
                    logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" requested photo and name");
                    appConfigurationsBean.setLastActivityDate(token);
                    return Response.status(200).entity("{\"photoUrl\":\"" + photoUrl + "\", \"name\":\"" + name + "\", \"role\":\"" + role + "\"}").build();}
                return Response.status(404).entity("No photo found").build();
            } else return Response.status(401).entity("Session has expired").build();
        } else return Response.status(403).entity("Access denied").build();
    }

    @GET
    @Path("/photoandname/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPhoto(@HeaderParam("token")String token, @PathParam("username")String username) throws UnknownHostException {
        if (userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                    UserPhotoDto userPhotoDto=userBean.getUserPhotoDtoByUsername(username);
                    appConfigurationsBean.setLastActivityDate(token);
                logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" requested photo and name");
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
    public Response userInfo(@HeaderParam("username") String username, @HeaderParam("token")String token) throws UnknownHostException {
        if(userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                if (userBean.checkIfUserExists(username)) {
                    UserEntity userEntity = userBean.getUserByUsername(username);
                    UserWithNoPassword userWithoutPassword = userBean.convertUserEntityToUserWithNoPassword(userEntity);
                    appConfigurationsBean.setLastActivityDate(token);
                    logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" requested information from "+username);
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
    public Response userInfoByusername(@HeaderParam("token") String token , @PathParam("username") String username) throws UnknownHostException {
        if(username == null)
            return Response.status(400).entity("Invalid Data").build();
        else if (userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                if (permissionBean.getPermission(token, Function.GET_OTHER_USER_INFO)) {
                    UserEntity userEntity = userBean.getUserByUsername(username);
                    UserWithNoPassword userWithoutPassword = userBean.convertUserEntityToUserWithNoPassword(userEntity);
                    appConfigurationsBean.setLastActivityDate(token);
                    logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" requested information from "+username);
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
     public Response getAllUsers(@HeaderParam("token") String token) throws UnknownHostException {
         if (userBean.tokenValidator(token)) {
             if(appConfigurationsBean.validateTimeout(token)) {
                 appConfigurationsBean.setLastActivityDate(token);
                 logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" requested all users");
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
    public Response editUserData(User updatedUser, @HeaderParam("token") String token) throws UnknownHostException {
        if (userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                if (permissionBean.getPermission(token, Function.EDIT_OWN_USER_INFO)) {
                    if (userValidator.validateUserOnEdit(updatedUser) && updatedUser.getUsername() == null) {
                        if (!userBean.checkIfUserExists(updatedUser)) {
                            boolean updateResult = userBean.updateUser(token, updatedUser);
                            if (updateResult){
                                appConfigurationsBean.setLastActivityDate(token);
                                logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" edited user "+updatedUser.getUsername());
                                return Response.status(200).entity("User data updated successfully").build();}
                            else {
                                logger.error(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" failed edit user "+updatedUser.getUsername());
                                return Response.status(500).entity("An error occurred while updating user data").build();
                            }
                        }
                        logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" failed edit user "+updatedUser.getUsername());
                        return Response.status(409).entity("Username or Email already Exists").build();
                    }
                    logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" failed edit user "+updatedUser.getUsername());
                    return Response.status(400).entity("Invalid Data").build();
                }
                logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" access denied for edit "+updatedUser.getUsername());
                return Response.status(403).entity("Access denied").build();
            }else return Response.status(401).entity("Session has expired").build();
        }return Response.status(401).entity("Login Failed").build();
    }


    @PATCH
    @Path("/confirmed/{confirmed}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response confirmUser(@PathParam("confirmed") boolean confirmed, @HeaderParam("pass") String pass,@HeaderParam("token") String token) throws UnknownHostException {
        if (userBean.auxiliarTokenValidator(token)) {
            boolean updatePass = userBean.updatePassWord(token, pass);
            boolean updateResult = userBean.updateUserConfirmed(token,confirmed);
            if (updateResult && updatePass){
                try {
                    dashboardWebSocket.send();
                } catch (IOException e) {}
                logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByAuxiliarToken(token)+" confirmed account ");
                return Response.status(200).entity("User data updated successfully").build();
            }
            else {
                logger.error(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByAuxiliarToken(token)+" failed account confirmation ");
                return Response.status(500).entity("An error occurred while updating user data").build();
            }
        }{
            logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied in account confirmation ");
            return Response.status(401).entity("Access denied").build();}
    }

    @POST
    @Path("/newpassemail")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response newPassEmail(@HeaderParam("email") String email) throws UnknownHostException {
        if (userValidator.validateEmail(email)) {
            if(userBean.checkIfemailExists(email)) {
                if (userBean.sendResetPassMail(email)) {
                    logger.info(InetAddress.getLocalHost().getHostAddress()+ " requested a new pass ");
                    return Response.status(200).entity("Mail sent successfully").build();
                } else {
                    logger.error(InetAddress.getLocalHost().getHostAddress()+ " failed the request for a new pass ");
                    return Response.status(400).entity("Error sending mail").build();
                }
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+ " inserted an invalid email for the request for a new pass ");
                return Response.status(404).entity("Email not found").build();
            }
        }else {
            logger.warn(InetAddress.getLocalHost().getHostAddress()+ " inserted an invalid email for the request for a new pass ");
            return Response.status(406).entity("Email not valid").build();
        }
    }

    @GET
    @Path("/auxiliartokenvalidator")
    @Produces(MediaType.APPLICATION_JSON)
    public Response auxiliarTokenValidator(@HeaderParam("token") String token) throws UnknownHostException {
        if (userBean.auxiliarTokenValidator(token)) {
            return Response.status(200).entity("Token was validated").build();
        }else return Response.status(404).entity("Token not valid").build();
    }


    @POST
    @Path("/recoverpassword")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response recoverPassword(@HeaderParam("newPass")String newPass, @HeaderParam("token") String token) throws UnknownHostException {
        if (userBean.auxiliarTokenValidator(token)) {
            if (userValidator.validatePassword(newPass)) {
                if(userBean.isConfirmed(token)) {
                    boolean updateResult = userBean.updatePassWord(token, newPass);
                    if (updateResult) {
                        userBean.clearToken(token);
                        logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByAuxiliarToken(token) +" update the password ");
                        return Response.status(200).entity("User password updated successfully").build();
                    } else {
                        logger.error(InetAddress.getLocalHost().getHostAddress()+ "  "+userBean.findUsernameByAuxiliarToken(token)+" failed updating password ");
                        return Response.status(500).entity("An error occurred while updating user password").build();}
                } else {
                    logger.warn(InetAddress.getLocalHost().getHostAddress()+ "  "+userBean.findUsernameByAuxiliarToken(token)+" tried to update password but didn't have the account confirmed ");
                    return Response.status(404).entity("User still not confirmed").build();}
            } else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+ "  "+userBean.findUsernameByAuxiliarToken(token)+" failed updating password ");
                return Response.status(400).entity("Invalid Data").build();}
        }else{
            logger.warn(InetAddress.getLocalHost().getHostAddress()+ " had access denied trying to update password ");
            return Response.status(401).entity("Access denied").build();}
    }

    @POST
    @Path("/resendemail")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response resendEmail(@HeaderParam("token") String token) throws UnknownHostException {
        if(userBean.auxiliarTokenValidator(token)){
            UserResendEmail user=userBean.getUserResendEmailByToken(token);
            String newToken=userBean.setNewToken(token);
            if(userBean.sendNewEmail(user.getFirstName(),user.getEmail(),newToken)){
                logger.info(InetAddress.getLocalHost().getHostAddress()+ "  "+userBean.findUsernameByAuxiliarToken(token)+" requested another email for account confirmation ");
                return Response.status(200).entity("Email was sent").build();
            }
            else {
                logger.error(InetAddress.getLocalHost().getHostAddress()+ "  "+userBean.findUsernameByAuxiliarToken(token)+" requested another email for account confirmation but occurred an error ");
                return Response.status(401).entity("There was an error sending the email").build();
            }
        }
        else {
            logger.warn(InetAddress.getLocalHost().getHostAddress()+ " had access denied trying to update password ");
            return Response.status(401).entity("Access denied").build();
        }
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
                                      @HeaderParam("userToChangeUsername") String username) throws UnknownHostException {
        if (userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                if (permissionBean.getPermission(token, Function.EDIT_OTHER_USER_INFO)) {
                    if (userValidator.validateUserOnEdit(updatedUser) && updatedUser.getUsername() == null) {
                        if (userBean.checkIfUserExists(username)) {
                            if (!userBean.checkIfemailExists(updatedUser)) {
                                boolean updateResult = userBean.updateUserByUsername(token, username, updatedUser);
                                if (updateResult){
                                    appConfigurationsBean.setLastActivityDate(token);
                                    logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+ " updated the user "+username);
                                    return Response.status(200).entity("User data updated successfully").build();}
                                else {
                                    logger.error(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+ " tried to update "+username+" but an error occurred");
                                    return Response.status(400).entity("An error occurred while updating user data").build();
                                }
                            }
                            logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+ " failed to update "+username);
                            return Response.status(409).entity("Username or Email already Exists").build();
                        }
                        logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+ " failed to update "+username);
                        return Response.status(409).entity("Username do not Exists").build();
                    }
                    logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+ " failed to update "+username);
                    return Response.status(400).entity("Invalid Data").build();
                }
                logger.warn(InetAddress.getLocalHost().getHostAddress()+ " had access denied trying to update user "+username);
                return Response.status(403).entity("Access denied").build();

            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+ " had session expired when trying to update "+username);
                return Response.status(401).entity("Session has expired").build();}
        }
         logger.warn(InetAddress.getLocalHost().getHostAddress()+ " had access denied when trying to update "+username);
        return Response.status(401).entity("Login Failed").build();
    }


    /**
     * Enables a user to update their password. It requires the old password for verification
     * and checks if the new password meets the system's security requirements. This endpoint
     * is crucial for maintaining user account security.
     */
    @POST
    @Path("/password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editUserPassword(UserNewPassword updatedPassword, @HeaderParam("token") String token) throws UnknownHostException {
        if (userBean.tokenValidator(token) && userBean.oldPasswordConfirmation(token, updatedPassword.getPassword(),
                updatedPassword.getNewPassword())) {
            if(appConfigurationsBean.validateTimeout(token)) {
                if (userValidator.validatePassword(updatedPassword.getNewPassword())) {
                    boolean updateResult = userBean.updatePassWord(token, updatedPassword.getNewPassword());
                    if (updateResult){
                        appConfigurationsBean.setLastActivityDate(token);
                        logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token) +" updated the password");
                        return Response.status(200).entity("User password updated successfully").build();}
                    else {
                        logger.error(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token) +" had an error updating the password");
                        return Response.status(500).entity("An error occurred while updating user password").build();}
                }
                logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token) +" failed updating the password");
                return Response.status(400).entity("Invalid Data").build();
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+ " had session expired when trying to update the password");
                return Response.status(401).entity("Session has expired").build();
            }
        }
        logger.warn(InetAddress.getLocalHost().getHostAddress()+ " had access denied when trying to update the password");
        return Response.status(401).entity("Login Failed, Passwords do not match or New password must be different from the old password").build();
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
    public Response deleteUserPermanently(@HeaderParam("token")String token, @HeaderParam("userToDeleteUsername")String username) throws UnknownHostException {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                if (permissionBean.getPermission(token, Function.PERMANENTLY_USER_DELET)) {
                    if (userBean.checkIfUserExists(username)) {
                        if (!username.equals("admin") && !username.equals("deletedTasks")) {
                            userBean.transferTasks(username);
                            userBean.transferCategories(username);
                            messageBean.deleteMessagesByUser(username);
                            boolean successfullyDeleted = userBean.deleteUserPermanetely(username);
                            if (successfullyDeleted){
                                appConfigurationsBean.setLastActivityDate(token);
                                try {
                                    dashboardWebSocket.send();
                                } catch (IOException e) {}
                                logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+ " deleted user "+username);
                                return Response.status(200).entity("This user permanently deleted ").build();}
                            else {
                                logger.error(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+ " had an error deleting user "+username);
                                return Response.status(400).entity("User not deleted").build();
                            }
                        } else {
                            logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+ " tried to delete admin unsuccessfully");
                            return Response.status(400).entity("Admin can't be deleted.").build();}
                    } else{
                        logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+ " tried to delete an user that doesn't exist");
                        return Response.status(400).entity("User with this id not found").build();
                    }
                } else {
                    logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+ " tried to delete "+username+" without permissions");
                    return Response.status(403).entity("User permissions violated").build();
                }
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when trying to delete "+username);
                return Response.status(401).entity("Session has expired").build();
            }
        } else {
            logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when tring to delete "+username);
            return Response.status(403).entity("User permissions violated").build();}
    }



    /**
     * This endpoint makes logging out a user. Since this example does not
     * manage user sessions or authentication tokens explicitly, the endpoint simply returns
     * a response indicating that the user has been logged out successfully.
     *  */
    @POST
    @Path("/logout")
    public Response logout(@HeaderParam("token")String token) throws UnknownHostException {
        if (token == null) {
            logger.warn(InetAddress.getLocalHost().getHostAddress()+" tried to logout unsuccessfully ");
            return Response.status(422).entity("Missing token").build();
        }
        logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+ " logged out ");
        userBean.logout(token);
        return Response.status(200).entity("User logged out successfully").build();
    }

    @GET
    @Path("/configuration/{name}")
    public Response getConfigurationValue(@HeaderParam("token")String token, @PathParam("name")String name) throws UnknownHostException {
      if (userBean.tokenValidator(token)) {
          if(appConfigurationsBean.validateTimeout(token)) {
              logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+ " requested for the "+name+" configuration information");
              return Response.status(200).entity(appConfigurationsBean.getConfigurationValueByName(name)).build();
          }else {
              logger.warn(InetAddress.getLocalHost().getHostAddress()+ " had session expired when requesting for the "+name+" configuration information");
              return Response.status(401).entity("Session has expired").build();
          }
      }else {
          logger.warn(InetAddress.getLocalHost().getHostAddress()+ " had access denied when requesting for the "+name+" configuration information");
          return Response.status(403).entity("Access denied").build();
      }
    }

    @PATCH
    @Path("/configuration/{name}/{value}")
    public Response getConfigurationValue(@HeaderParam("token")String token, @PathParam("name")String name, @PathParam("value")String value) throws UnknownHostException {
      long valueLong=0;
      try {
          valueLong = Long.parseLong(value);
      }catch (NumberFormatException e){
           return Response.status(400).entity("Invalid data").build();
      }

        if (userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                if(appConfigurationsBean.setConfigurationValue(valueLong,name)) {
                    logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+ " updated the "+name+" configuration");
                    return Response.status(200).entity("Configurations updated successfully").build();
                }else {
                    logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token) +" failed updating "+name+" configuration");
                    return Response.status(400).entity("Invalid data").build();
                }
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+ " had session expired when trying to update the "+name+" configuration");
                return Response.status(401).entity("Session has expired").build();
            }
        }else {
            logger.warn(InetAddress.getLocalHost().getHostAddress()+ " had access denied when trying to update the "+name+" configuration");
            return Response.status(403).entity("Access denied").build();
        }
    }
}