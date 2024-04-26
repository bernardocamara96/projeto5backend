package aor.paj.service;

import aor.paj.bean.AppConfigurationsBean;
import aor.paj.bean.CategoryBean;
import aor.paj.bean.TaskBean;
import aor.paj.bean.UserBean;
import aor.paj.dao.CategoryDao;
import aor.paj.dto.CategoryDto;
import aor.paj.dto.CategoryStatsDto;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.UserEntity;
import aor.paj.service.status.userRoleManager;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.*;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

@Path("/categories")
public class CategoryService {

    @EJB
    UserBean userBean;
    @EJB
    CategoryBean categoryBean;
    @EJB
    TaskBean taskBean;
    @EJB
    AppConfigurationsBean appConfigurationsBean;
    private static final Logger logger=LogManager.getLogger(CategoryService.class);

    /**
     *  retrieves information about a specific category identified by its ID
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCategories(@HeaderParam("token")String token) throws UnknownHostException {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" requested the list of categories");
                return Response.status(200).entity(categoryBean.ordenedCategoriesList()).build();
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when requesting the list of categories ");
                return Response.status(401).entity("Session has expired").build();
            }
        } else {
            logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when requesting the list of categories ");
            return Response.status(403).entity("User permissions violated").build();
        }
    }


    /**
     * method to add a new category
     */

    @POST
    @Path("/{type}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addCategory(@PathParam("type")String type, @HeaderParam("token") String token) throws UnknownHostException {
        if(userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                UserEntity user = userBean.getUserByToken(token);
                if (user.getRole().equals(userRoleManager.PRODUCT_OWNER)) {
                    CategoryDto categoryDto = categoryBean.addCategory(user, type);
                    if (categoryDto != null) {
                        logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" created a new category named "+type);
                        return Response.status(200).entity(categoryDto).build();
                    } else {
                        logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" tried to create a category that already exists");
                        return Response.status(400).entity("That category type already exists").build();}
                } else {
                    logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" tried to create a new category without permissions");
                    return Response.status(403).entity("User permissions violated").build();
                }
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when trying to create a new category");
                return Response.status(401).entity("Session has expired").build();
            }
        } else {
            logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when trying to create a new category ");
            return Response.status(403).entity("Access denied").build();
        }
    }


    /**
     * method to edit the type of the category
     */

    @PATCH
    @Path("/{oldType}/{newType}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editCategory (@PathParam("newType") String newType,@PathParam("oldType")String oldType, @HeaderParam("token")String token) throws UnknownHostException {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                if (userBean.getRoleByToken(token).equals(userRoleManager.PRODUCT_OWNER)) {
                    if (categoryBean.categoryTypeValidator(oldType)) {
                        if (categoryBean.editCategory(newType, oldType)) {
                            logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" edited the category "+oldType+" to "+newType);
                            return Response.status(200).entity("The category was edited successfully").build();
                        } else {
                            logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" tried to edit the category "+oldType+" to a type of category that already exists");
                            return Response.status(400).entity("That category type already exists").build();
                        }
                    } else {
                        logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" tried to edit a category that doesn't exist");
                        return Response.status(400).entity("Category with this type not found").build();
                    }
                } else {
                    logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" treid to edit the category "+oldType+" to "+newType+" without permissions");
                    return Response.status(403).entity("User permissions violated").build();
                }
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when trying to edit the category "+oldType+" to "+newType);
                return Response.status(401).entity("Session has expired").build();
            }
        }else {
            logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when trying to edit the category "+oldType+" to "+newType);
            return Response.status(403).entity("User permissions violated").build();
        }
    }


    /**
     * method to delete a category
     */
    @DELETE
    @Path("/{type}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteCaTEGORY(@PathParam("type")String type, @HeaderParam("token")String token) throws UnknownHostException {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                if (userBean.getRoleByToken(token).equals(userRoleManager.PRODUCT_OWNER)) {
                    if (!categoryBean.categoryWithTasks(type)) {
                        if (categoryBean.deleteCategory(type)) {
                            logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" deleted the category "+type);
                            return Response.status(200).entity("The category was successfully deleted").build();
                        } else {
                            logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" tried to delete a category that doesn't exist");
                            return Response.status(404).entity("That category doesn't exists").build();
                        }
                    } else {
                        logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" tried to delete a category with tasks associated");
                        return Response.status(400).entity("That category has tasks associated").build();
                    }
                } else {
                    logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" tried to delete a category without permissions");
                    return Response.status(403).entity("User permissions violated").build();
                }
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when trying to delete the category "+type);
                return Response.status(401).entity("Session has expired").build();
            }
        }else {
            logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when trying to delete the category "+type);
            return Response.status(403).entity("User permissions violated").build();
        }
    }

    /**
     * method to verify if a user has permission to edit and delete a category
     */
    @GET
    @Path("/permission")
    @Produces(MediaType.APPLICATION_JSON)
    public Response userPermissionToEdit(@HeaderParam("token")String token) {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                if (userBean.getUserByToken(token).getRole().equals(userRoleManager.PRODUCT_OWNER)) {
                    return Response.status(200).entity(true).build();
                } else return Response.status(403).entity("User permissions violated").build();
            }else return Response.status(401).entity("Session has expired").build();
        }else return Response.status(403).entity("User permissions violated").build();
    }

    /**
     * method that retrieves the number of tasks of the specified category
     */
    @GET
    @Path("/tasksnumber/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response numberOfTasksByCategory(@HeaderParam("token") String token,@PathParam("type")String category_type) throws UnknownHostException {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                if (userBean.getRoleByToken(token).equals(userRoleManager.PRODUCT_OWNER) || userBean.getRoleByToken(token).equals(userRoleManager.SCRUM_MASTER)) {
                    if (categoryBean.categoryTypeValidator(category_type)) {
                        logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" requested the number of tasks with category "+category_type);
                        return Response.status(200).entity(taskBean.getAllTasksByCategory(category_type).size()).build();
                    } else {
                        logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" requested the number of tasks of a category that doesn't exist ");
                        return Response.status(404).entity("This category type doesn't exist").build();
                    }
                } else {
                    logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when requesting the number of tasks associated with the category "+category_type);
                    return Response.status(403).entity("User permissions violated").build();
                }
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when requesting the number of tasks associated with the category "+category_type);
                return Response.status(401).entity("Session has expired").build();
            }
        }else {
            logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when requesting the number of tasks associated with the category"+category_type);
            return Response.status(403).entity("User permissions violated").build();
        }
    }
}
