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

    /**
     *  retrieves information about a specific category identified by its ID
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCategories(@HeaderParam("token")String token){
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                return Response.status(200).entity(categoryBean.ordenedCategoriesList()).build();
            }else return Response.status(401).entity("Session has expired").build();
        } else return Response.status(403).entity("User permissions violated").build();
    }


    /**
     * method to add a new category
     */

    @POST
    @Path("/{type}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addCategory(@PathParam("type")String type, @HeaderParam("token") String token){
        if(userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                UserEntity user = userBean.getUserByToken(token);
                if (user.getRole().equals(userRoleManager.PRODUCT_OWNER)) {
                    CategoryDto categoryDto = categoryBean.addCategory(user, type);
                    if (categoryDto != null) {

                        return Response.status(200).entity(categoryDto).build();
                    } else return Response.status(400).entity("That category type already exists").build();
                } else return Response.status(403).entity("User permissions violated").build();
            }else return Response.status(401).entity("Session has expired").build();
        } else return Response.status(403).entity("Access denied").build();
    }


    /**
     * method to edit the type of the category
     */

    @PATCH
    @Path("/{oldType}/{newType}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editCategory (@PathParam("newType") String newType,@PathParam("oldType")String oldType, @HeaderParam("token")String token){
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                if (userBean.getRoleByToken(token).equals(userRoleManager.PRODUCT_OWNER)) {
                    if (categoryBean.categoryTypeValidator(oldType)) {
                        if (categoryBean.editCategory(newType, oldType)) {
                            return Response.status(200).entity("The category was edited successfully").build();
                        } else return Response.status(400).entity("That category type already exists").build();
                    } else return Response.status(400).entity("Category with this type not found").build();
                } else return Response.status(403).entity("User permissions violated").build();
            }else return Response.status(401).entity("Session has expired").build();
        }else return Response.status(403).entity("User permissions violated").build();
    }


    /**
     * method to delete a category
     */
    @DELETE
    @Path("/{type}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteCaTEGORY(@PathParam("type")String type, @HeaderParam("token")String token){
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                if (userBean.getRoleByToken(token).equals(userRoleManager.PRODUCT_OWNER)) {
                    if (!categoryBean.categoryWithTasks(type)) {
                        if (categoryBean.deleteCategory(type)) {
                            return Response.status(200).entity("The category was successfully deleted").build();
                        } else return Response.status(404).entity("That category doesn't exists").build();
                    } else return Response.status(400).entity("That category has tasks associated").build();
                } else return Response.status(403).entity("User permissions violated").build();
            }else return Response.status(401).entity("Session has expired").build();
        }else return Response.status(403).entity("User permissions violated").build();
    }

    /**
     * method to verify if a user has permission to edit and delete a category
     */
    @GET
    @Path("/permission")
    @Produces(MediaType.APPLICATION_JSON)
    public Response userPermissionToEdit(@HeaderParam("token")String token){
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
    public Response numberOfTasksByCategory(@HeaderParam("token") String token,@PathParam("type")String category_type){
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                if (userBean.getRoleByToken(token).equals(userRoleManager.PRODUCT_OWNER) || userBean.getRoleByToken(token).equals(userRoleManager.SCRUM_MASTER)) {
                    if (categoryBean.categoryTypeValidator(category_type)) {
                        return Response.status(200).entity(taskBean.getAllTasksByCategory(category_type).size()).build();
                    } else return Response.status(404).entity("This category type doesn't exist").build();
                } else return Response.status(403).entity("User permissions violated").build();
            }else return Response.status(401).entity("Session has expired").build();
        }else return Response.status(403).entity("User permissions violated").build();
    }
}
