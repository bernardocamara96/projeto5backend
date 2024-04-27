
package aor.paj.service;
import aor.paj.bean.AppConfigurationsBean;
import aor.paj.bean.PermissionBean;
import aor.paj.bean.TaskBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.TaskDto;
import aor.paj.dto.TaskStatusDto;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import aor.paj.service.status.Function;
import aor.paj.service.status.userRoleManager;
import aor.paj.service.validator.TaskValidator;
import aor.paj.websocket.CategoriesWebSocket;
import aor.paj.websocket.DashboardWebSocket;
import aor.paj.websocket.TasksWebSocket;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.annotations.CollectionIdJavaType;
import org.apache.logging.log4j.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;


@Path("/tasks")
public class TaskService {
    @EJB
    TaskBean taskBean;
    @Inject
    TaskValidator taskValidator;
    @EJB
    UserBean userBean;
    @EJB
    PermissionBean permissionBean;
    @EJB
    AppConfigurationsBean appConfigurationsBean;
    @Inject
    DashboardWebSocket dashboardWebSocket;
    @Inject
    TasksWebSocket tasksWebSocket;

    private static final Logger logger=LogManager.getLogger(TaskService.class);

    /**
     * creates a new task with a category with the name "type"
     */
    @POST
    @Path("/{type}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createTask(@HeaderParam("token") String token,@PathParam("type")String categoryType, TaskDto a) throws IOException {
        if (userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                if (taskValidator.validateTask(a)) {
                    if (taskBean.addTask(token, categoryType, a)) {
                        try {
                            dashboardWebSocket.send();
                            tasksWebSocket.sendNewTask(a);
                        }catch (IOException e){
                            return Response.status(400).entity("Websocket error").build();
                        }
                        logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" created a new task with the title "+a.getTitle());
                        return Response.status(201).entity("A new task has been created").build();
                    }
                    logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" tried to create a task with an invalid category");
                    return Response.status(400).entity("Invalid task type").build();
                }
                logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" tried to create a task with an invalid data");
                return Response.status(400).entity("Invalid task data").build();
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expiered when trying to create a task");
                return Response.status(401).entity("Session has expired").build();
            }

        }
        logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when trying to create a task");
        return Response.status(403).entity("User permissions violated").build();
    }

    /**
     *method to edit a task with the specified id
     */
    @PATCH
    @Path("/edit/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editTask( @PathParam("id")int id, @HeaderParam("token") String token, TaskDto taskDto) throws IOException {
        if (userBean.tokenValidator(token)) {
           if(appConfigurationsBean.validateTimeout(token)) {
               if (taskBean.taskIdValidator(id)) {
                   if (permissionBean.getPermissionByTaskID(token, id)) {
                       taskDto.setStatus(100);
                       if (taskBean.editTask(id, taskDto)) {
                           try {
                               taskDto.setId(id);
                               tasksWebSocket.sendEditTask(taskDto);
                           }catch(IOException e) {
                               logger.error(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" had an websocket error when tried to update a task");
                               return Response.status(200).entity("Error in websocket").build();}
                           logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" updated the task with the title "+taskDto.getTitle());
                           return Response.status(200).entity("Task updated successfuly.").build();
                       } else {
                           logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" tried to update a task with wrong data");
                           return Response.status(400).entity("Wrong data.").build();
                       }
                   } else {
                       logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" had access denied when tried to update a task with the title "+taskDto.getTitle());
                       return Response.status(403).entity("Access Denied").build();
                   }
               } else {
                   logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" tried to update a task with an id that doesn't exist");
                   return Response.status(404).entity("Task with this id not found").build();
               }
           }else{
               logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when tried to update a task");
               return Response.status(401).entity("Session has expired").build();
           }
        } else {
            logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when tried to update a task");
            return Response.status(403).entity("User permissions violated").build();
        }
    }

    /**
     * retrieves all tasks
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTasks(@HeaderParam("token") String token) throws UnknownHostException {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                if (permissionBean.getPermission(token, Function.GET_ALL_TASKS)) {
                    ArrayList<TaskEntity> tasksEntities = taskBean.getAllTasks();
                    ArrayList<TaskDto> tasksDtos = new ArrayList<>();
                    for (TaskEntity task : tasksEntities) {
                        TaskDto taskDto = taskBean.convertTaskEntitytoTaskDto(task);
                        tasksDtos.add(taskDto);
                    }
                    logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" requested all tasks");
                    return Response.status(200).entity(tasksDtos).build();
                }
                logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" tried to request all tasks without permissions");
                return Response.status(403).entity("User permissions violated").build();
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" had session expired when tried to request all tasks");
                return Response.status(401).entity("Session has expired").build();
            }

        }
        logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" had access denied when tried to request all tasks");
        return Response.status(403).entity("User not logged").build();
    }

    /**
     * Get Task by  ID
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTaskById(@PathParam("id") int id, @HeaderParam("token") String token) throws UnknownHostException {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                if (taskBean.taskIdValidator(id)) {
                    logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" requested the information about a task");
                    return Response.status(200).entity(taskBean.convertTaskEntitytoTaskDto(taskBean.getTaskById(id))).build();
                } else{
                    logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" requested information about a task with a id that doesn't exist");
                    return Response.status(404).entity("Task with this id not found").build();
                }
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" had session expired when tried to request information about a task");
                return Response.status(401).entity("Session has expired").build();
            }
        }else {
            logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" had access denied when tried to request information about a task");
            return Response.status(403).entity("User permissions violated").build();
        }
    }

    /**
     * retrieves all tasks that aren't permanently deleted
     */
    @GET
    @Path("/allnotdeleted")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTasksNotDeleted(@HeaderParam("token") String token) throws UnknownHostException {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                ArrayList<TaskEntity> tasksEntities = taskBean.getAllTasksByDeleted(false);
                ArrayList<TaskDto> tasksDtos = new ArrayList<>();
                for (TaskEntity task : tasksEntities) {
                    tasksDtos.add(taskBean.convertTaskEntitytoTaskDto(task));
                }
                logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" requested all tasks that are not deleted");
                return Response.status(200).entity(tasksDtos).build();
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when tried to request all tasks that are not deleted");
                return Response.status(401).entity("Session has expired").build();
            }
        }
        else {
            logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when tried to request all tasks that are not deleted");
            return Response.status(403).entity("User permissions violated").build();
        }
    }

    /**
     * retrieves all tasks that are permanently deleted
     */
    @GET
    @Path("/alldeleted")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTasksDeleted(@HeaderParam("token") String token) throws UnknownHostException {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                if (permissionBean.getPermission(token, Function.GET_ALL_TASKS_DELETED)) {
                    ArrayList<TaskEntity> tasksEntities = taskBean.getAllTasksByDeleted(true);
                    ArrayList<TaskDto> tasksDtos = new ArrayList<>();
                    for (TaskEntity task : tasksEntities) {
                        tasksDtos.add(taskBean.convertTaskEntitytoTaskDto(task));
                    }
                    logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" requested all deleted tasks ");
                    return Response.status(200).entity(tasksDtos).build();
                }
                logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" had access denied when requested all deleted tasks ");
                return Response.status(403).entity("User permissions violated").build();
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when requested all deleted tasks");
                return Response.status(401).entity("Session has expired").build();
            }
        }
        logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when requested all deleted tasks ");
        return Response.status(403).entity("User permissions violated").build();
    }


    /**
     *retrieves the number of tasks with the specified username as author
     */
    @GET
    @Path("/number/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTasksNumberByUser(@HeaderParam("token") String token,@PathParam("username") String username) throws UnknownHostException {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                if (permissionBean.getPermission(token, Function.GET_ALL_TASKS_BY_USER)) {
                    ArrayList<TaskEntity> tasksEntities = taskBean.getAllTasksByUsername(username);
                    ArrayList<TaskDto> tasksDtos = new ArrayList<>();
                    for (TaskEntity task : tasksEntities) {
                        if (!task.isDeleted()) {
                            tasksDtos.add(taskBean.convertTaskEntitytoTaskDto(task));
                        }
                    }
                    logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" requested the number of tasks from "+username);
                    return Response.status(200).entity(tasksDtos.size()).build();
                }
                logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" had access denied when requested the number of tasks from "+username);
                return Response.status(200).entity("User permissions violated").build();
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when requested the number of tasks from "+username);
                return Response.status(401).entity("Session has expired").build();
            }
        }
        logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when requested the number of tasks from "+username);
        return Response.status(403).entity("User permissions violated").build();
    }

    /**
     * retrieves all tasks from the specified user
     */
    @GET
    @Path("/byuser")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTasksByUser(@HeaderParam("token") String token,@QueryParam("username") String username) throws UnknownHostException {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                if (permissionBean.getPermission(token, Function.GET_ALL_TASKS_BY_USER)) {
                    ArrayList<TaskEntity> tasksEntities = taskBean.getAllTasksByUsername(username);
                    ArrayList<TaskDto> tasksDtos = new ArrayList<>();
                    for (TaskEntity task : tasksEntities) {
                        if (!task.isDeleted()) {
                            tasksDtos.add(taskBean.convertTaskEntitytoTaskDto(task));
                        }
                    }
                    logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" requested the tasks from "+username);
                    return Response.status(200).entity(tasksDtos).build();
                }
                logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" had access denied when requested the tasks from "+username);
                return Response.status(200).entity("User permissions violated").build();
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when requested the tasks from "+username);
                return Response.status(401).entity("Session has expired").build();
            }
        }
        logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when requested the tasks from "+username);
        return Response.status(403).entity("User permissions violated").build();
    }


    /**
     * retrieves all tasks with the specified category
     */
    @GET
    @Path("/bycategory")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTasksByCategory(@HeaderParam("token")String token, @QueryParam("category")String category_type) throws UnknownHostException {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                if (permissionBean.getPermission(token, Function.GET_ALL_TASKS_BY_CATEGORY)) {
                    ArrayList<TaskEntity> tasksEntities = taskBean.getAllTasksByCategory(category_type);
                    ArrayList<TaskDto> tasksDtos = new ArrayList<>();
                    for (TaskEntity task : tasksEntities) {
                        if (!task.isDeleted()) {
                            tasksDtos.add(taskBean.convertTaskEntitytoTaskDto(task));
                        }
                    }
                    logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" requested all tasks with the category "+category_type);
                    return Response.status(200).entity(tasksDtos).build();
                }
                logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" had access denied when requested all tasks with the category "+category_type);
                return Response.status(403).entity("User permissions violated").build();
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when requested all tasks with the category "+category_type);
                return Response.status(401).entity("Session has expired").build();
            }
        }
        logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when requested all tasks with the category "+category_type);
        return Response.status(403).entity("User permissions violated").build();
    }

    /**
     * retrieves all tasks with the specified username and category
     */
    @GET
    @Path("/byuserandcategory")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTasksByUserAndCategory(@HeaderParam("token")String token,@QueryParam("username")String username,@QueryParam("category")String category) throws UnknownHostException {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)){
                if(permissionBean.getPermission(token, Function.GET_ALL_TASKS_BY_CATEGORY_AND_USER)) {
                    if (userBean.getUserByUsername(username) != null) {
                        logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" requested the tasks from the user "+username+" and with the category "+category);
                        return Response.status(200).entity(taskBean.getTaskByUsernameAndCategory(category, username)).build();
                    }
                    logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" tried to request the tasks from a user that doesn't exist");
                    return Response.status(400).entity("Doesn't exist any User with that Username").build();
                }
                logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" had access denied when tried to request the tasks from the user "+username+" with the category "+category);
                return Response.status(403).entity("User permissions violated").build();
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when treid to request the tasks from the user "+username+" with the category "+category);
                return Response.status(401).entity("Session has expired").build();
            }
        }
        logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when tried to request the tasks from the user "+username+" with the category "+category);
        return Response.status(403).entity("User permissions violated").build();
    }

    /**
     *changes the status from the specified task
     */
    @PATCH
    @Path("/status/{id}/{status}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeTaskStatus(@PathParam("id")int id,@HeaderParam("token")String token, @PathParam("status")int status) throws UnknownHostException {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                if (taskBean.taskIdValidator(id)) {
                    if (taskBean.validateStatus(status)) {
                        if (taskBean.changeStatus(status, id)) {
                            try {
                                dashboardWebSocket.send();
                                tasksWebSocket.sendStatusTask(new TaskStatusDto(id,status));
                            } catch (IOException e) {
                                logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" had an websocket error when changing the status of a task");
                            }
                            logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" changed the status of a task");
                            return Response.status(200).entity("Task status updated").build();
                        } else {
                            logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" tried change the status of task to the same status");
                            return Response.status(200).entity("Task is already with this status value").build();
                        }
                    } else {
                        logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" tried to change the status of a task to an invalid status");
                        return Response.status(400).entity("Status value is invalid").build();
                    }
                } else {
                    logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" tried change the status of a task with an id that doesn't exist");
                    return Response.status(404).entity("Task with this id not found").build();
                }
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when trying to change the status of a task");
                return Response.status(401).entity("Session has expired").build();
            }
        }else {
            logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when trying to change the status of a task");
            return Response.status(403).entity("User permissions violated").build();
        }
    }

    /**
     *deletes temporarily the specified task
     */
    @PATCH
    @Path("/deletetemp/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteTaskTemporarily(@PathParam("id")int id, @HeaderParam("token")String token) throws UnknownHostException {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                if (taskBean.taskIdValidator(id)) {
                    if (permissionBean.getPermissionByTaskID(token, id)) {
                        if (taskBean.deleteTemporarily(id)) {
                            try {
                                dashboardWebSocket.send();
                                tasksWebSocket.sendDeleteTempTask(taskBean.getTaskDto(id));
                            } catch (IOException e) {     logger.error(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" had an websocket error when deleting temporarily a task");}
                            logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" deleted a task");
                            return Response.status(200).entity("This task was successfully deleted").build();
                        } else {
                            logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" tried to delete a task that was already deleted");
                            return Response.status(400).entity("This task is already deleted").build();
                        }
                    } else {
                        logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" had access denied when tried to delete a task");
                        return Response.status(403).entity("User permissions violated").build();
                    }
                } else {
                    logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" tried to delete a task with an id that doesn't exist");
                    return Response.status(404).entity("Task with this id not found").build();
                }
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when tried to delete a task");
                return Response.status(401).entity("Session has expired").build();
            }
        } else {
            logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when tried to delete a task");
            return Response.status(403).entity("User permissions violated").build();
        }
    }

    /**
     *method to recycle the specified task
     */
    @PATCH
    @Path("/recycle/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response recycleTask(@PathParam("id")int id, @HeaderParam("token")String token) throws UnknownHostException {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                if (permissionBean.getPermission(token, Function.RECYCLY_TASK_BY_ID)) {
                    if (taskBean.taskIdValidator(id)) {
                        if (taskBean.recycleTask(id)) {
                            try {
                                dashboardWebSocket.send();
                                tasksWebSocket.sendRecycleTask(taskBean.getTaskDto(id));
                            } catch (IOException e) {
                                logger.error(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" had an websocket error when tried to recycle a task");
                            }
                            logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" recycle a task");
                            return Response.status(200).entity("This task was successfully recycled").build();
                        } else {
                            logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" tried to recycle a task that wasn't deleted");
                            return Response.status(400).entity("This task isn't deleted").build();
                        }
                    } else {
                        logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" tried to recycle a task with an id that doesn't exist");
                        return Response.status(404).entity("Task with this id not found").build();
                    }
                } else {
                    logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" had access denied when tried to recycle a task");
                    return Response.status(403).entity("User permissions violated").build();
                }
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when tried to recycle a task");
                return Response.status(401).entity("Session has expired").build();
            }
        } else {
            logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when tried to recycle a task");
            return Response.status(403).entity("User permissions violated").build();
        }
    }

    /**
     *method to delete temporarily all tasks from the specified user
     */
    @DELETE
    @Path("/temp/all/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteAllTasksTemporarily(@PathParam("username")String username,@HeaderParam("token")String token) throws UnknownHostException {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                if (permissionBean.getPermission(token, Function.DELETE_ALL_TASKS_BY_USER_TEMPORARILY)) {
                    if (userBean.getUserByUsername(username) != null) {
                        ArrayList<TaskDto> taskDtos=taskBean.deleteAllTasksByUser(userBean.getUserByUsername(username));
                        System.out.println(taskDtos);
                        try {
                            dashboardWebSocket.send();
                            tasksWebSocket.sendAllTempDeleteTasks(taskDtos);

                        } catch (IOException e) {     logger.error(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" had an websocket error when deleted all task from "+username);}
                        logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" deleted all tasks from "+username);
                        return Response.status(200).entity("Tasks sucecessfully deleted").build();
                    } else {
                        logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" tried to delete all task from an user that doesn't exist");
                        return Response.status(404).entity("User with this username not found").build();
                    }
                } else {
                    logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" had access denied when tried to delete all tasks from "+username);
                    return Response.status(403).entity("User permissions violated").build();
                }
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when tried to delete all tasks from "+username);
                return Response.status(401).entity("Session has expired").build();
            }
        }else {
            logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when tried to delete all tasks from "+username);
            return Response.status(403).entity("User permissions violated").build();
        }
    }


    /**
     *deletes permanently the specified task
     */
    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteTaskPermanently(@PathParam("id")int id, @HeaderParam("token")String token) throws UnknownHostException {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                if (permissionBean.getPermission(token, Function.DELETE_TASK_PERMANENTLY)) {
                    if (taskBean.taskIdValidator(id)) {
                        TaskDto taskDto=taskBean.getTaskDto(id);
                        boolean deleted = taskBean.deleteTaskPermanently(id);
                        if (deleted){
                            try {
                                dashboardWebSocket.send();
                                tasksWebSocket.sendPermDeleteTask(taskDto);
                            } catch (IOException e) {     logger.error(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" when permanently deleted a task");}
                            logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" permanently deleted a task");
                            return Response.status(200).entity("This task permanently deleted ").build();}
                        else {
                            logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" tried to permanently delete a task that was already deleted");
                            return Response.status(400).entity("This task is already deleted").build();
                        }
                    } else {
                        logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" tried to permanently delete a task with an id that doesn't exist");
                        return Response.status(400).entity("Task with this id not found").build();
                    }
                } else {
                    logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.getUsername(token)+" had access denied when tried to permanently deleted a task");
                    return Response.status(403).entity("User permissions violated").build();
                }
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when tried to permanently deleted a task");
                return Response.status(401).entity("Session has expired").build();
            }
        } else {
            logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when tried to permanently deleted a task");
            return Response.status(403).entity("User permissions violated").build();
        }
    }

    /**
     *method to retrieve if the user as permission to edit or delete a task
     */
    @GET
    @Path("/permission/{permissionType}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response userPermissionToEdit(@PathParam("id")int id, @HeaderParam("token")String token, @PathParam("permissionType")String permissionType){
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                if (taskBean.taskIdValidator(id)) {
                    if (permissionBean.getPermissionByTaskID(token, id) && permissionType.equals("edit")) {
                        return Response.status(200).entity(true).build();
                    }
                    if (taskBean.taskDeletePermission(token) && permissionType.equals("delete"))
                        return Response.status(200).entity(true).build();
                    else return Response.status(403).entity("Don't have permission to edit this task").build();
                } else return Response.status(400).entity("Task with this id not found").build();
            }else return Response.status(401).entity("Session has expired").build();
        }else return Response.status(403).entity("User permissions violated").build();
    }

    @GET
    @Path("/number/{username}/{status}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response tasksNumberByUsernameAndStatus(@PathParam("username") String username, @PathParam("status") String status, @HeaderParam("token")String token){

        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                if (status.equals("100") || status.equals("200") || status.equals("300") || status.equals("todo") || status.equals("doing") || status.equals("done")) {
                    status = (status.equals("todo")) ? "100" : (status.equals("doing")) ? "200" : (status.equals("done")) ? "300" : status;
                    ArrayList<TaskDto> tasks = taskBean.getTasksByUsernameAndStatus(username, status, false);
                    if (tasks != null) {
                        return Response.status(200).entity(tasks.size()).build();
                    } else return Response.status(404).entity("There is no user with that username").build();
                } else if (status.equals("total")) {
                    ArrayList<TaskDto> tasks = taskBean.getTasksByUsernameAndDeleted(username, false);
                    return Response.status(200).entity(tasks.size()).build();
                } else if (status.equals("deleted")) {
                    ArrayList<TaskDto> tasks = taskBean.getTasksByUsernameAndDeleted(username, true);
                    return Response.status(200).entity(tasks.size()).build();
                } else return Response.status(400).entity("Invalid task status").build();
            }else return Response.status(401).entity("Session has expired").build();
        }else return Response.status(403).entity("User permissions violated").build();

    }


//        TaskDto existingTask = taskBean.getTask(id);
//        if (existingTask == null) {
//            return Response.status(404).entity("Task not found").build();
//        }
//        if (!userBean.tokenValidator(token) ) {
//            return Response.status(403).entity("User permissions violated.").build();
//        }
//
//        // Persiste as alterações
//        taskBean.updateTask(id, taskUpdates);
//        return Response.status(200).entity("Task updated successfully").build();


    /**
     * Retrieves the details of a specific task identified by its ID. This endpoint requires user authentication
     * and ensures that a task can only be accessed by the user who created it or who has been granted permission.
     * Requires authentication through request headers "username" and "password". The task's ID is specified in the
     * URL path.
     * Responses:
     * - 401 Unauthorized: Indicates the user is not logged in or the username/password is not provided.
     * - 403 Forbidden: Occurs if the login credentials are incorrect or if the authenticated user does not have
     *   permission to view the task (i.e., attempting to access a task owned by another user).
     * - 404 Not Found: Returned if there is no task matching the provided ID in the system.
     * - 200 OK: Success response, indicating the task was found and the user has permissions to view it. The
     *   response body includes the task's details in JSON format.
     */


    /**
     * Retrieves all tasks associated with the authenticated user. This endpoint ensures that users can only access
     * their own tasks, maintaining privacy and security. Authentication is required, using the "username" and "password"
     * provided in the request headers.
     * Responses:
     * - 401 Unauthorized: Returned if either the username or password header is not provided, indicating that the user
     *   is not logged in or authentication details are missing.
     * - 403 Forbidden: Occurs if the authentication fails, either due to incorrect credentials or an attempt to access
     *   tasks associated with a different user, violating user permissions.
     * - 200 OK: Success response, indicating that the tasks associated with the authenticated user were successfully
     *   retrieved. The response body contains an array of tasks in JSON format, specific to the user.
     */


    /**
     * Allows an authenticated user to edit the details of a specific task identified by its ID. This endpoint
     * ensures that a user can only edit their own tasks, requiring both user authentication and authorization
     * based on task ownership. The task's new details are provided in the request body in JSON format.
     * Responses:
     * - 401 Unauthorized: Returned if the "username" or "password" header is not provided, indicating that the user
     *   is not logged in or authentication details are missing.
     * - 404 Not Found: Occurs if no task matching the provided ID exists in the system, indicating an invalid task ID.
     * - 403 Forbidden: Returned if the authentication fails, either due to incorrect credentials, or if the authenticated
     *   user does not own the task specified by the ID, preventing editing of tasks owned by other users.
     * - 200 OK: Success response, indicating that the task was successfully updated with the new details. A message
     *   confirming the successful update is included in the response body.
     */



    /**
     * Creates a new task associated with the authenticated user. This endpoint requires user authentication and checks
     * that the task being created belongs to the user making the request. The details of the task to be created are
     * provided in the request body in JSON format.
     * Responses:
     * - 401 Unauthorized: Returned if the "username" or "password" header is not provided, indicating that the user
     * is not logged in or authentication details are missing.
     * - 403 Forbidden: Occurs if the authenticated user does not match the username associated with the task being
     * created, preventing users from adding tasks on behalf of others.
     * - 400 Bad Request: Returned if the task data fails validation checks, indicating that the provided task
     * details are invalid or incomplete.
     * - 201 Created: Success response, indicating that the new task has been successfully created and stored. A
     * message confirming the creation of the task is included in the response body.
     */


        /**
         * Deletes a specific task identified by its ID, accessible only by the authenticated user who owns the task.
         * This endpoint requires user authentication, and authorization is verified to ensure that users can only
         * delete their own tasks. The task ID to be deleted is specified in the URL path.
         * Responses:
         * - 401 Unauthorized: Returned if either the "username" or "password" header is not provided, indicating
         *   that the user is not logged in or authentication details are missing.
         * - 404 Not Found: Occurs if no task matching the provided ID exists in the system, indicating an invalid
         *   task ID or that the task has already been deleted.
         * - 403 Forbidden: Returned in two scenarios: 1) if the authentication fails due to incorrect credentials,
         *   or 2) if the authenticated user does not own the task specified by the ID, preventing deletion of tasks
         *   owned by other users.
         * - 200 OK: Success response, indicating that the task was successfully deleted. A message confirming the
         *   successful deletion of the task is included in the response body.
         */


//    @DELETE
//    @Path("/delete/{id}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response removeTask(@HeaderParam("token") String token,
//                               @PathParam("id") String id) {
//        if (token == null)
//            return Response.status(401)
//                    .entity("User not logged in")
//                    .build();
//        else if (userBean.tokenValidator(token)) {
//            if (taskBean.getTask(Integer.parseInt(id)) == null)
//                return Response.status(404)
//                        .entity("Task not found")
//                        .build();
//            boolean deleted = taskBean.removeTask(Integer.parseInt(id));
//            if (deleted) return Response.status(200)
//                    .entity("Task deleted")
//                    .build();
//        } return Response.status(403)
//                .entity("User permissions violated. Can't delete tasks of other users")
//                .build();
//    }

}