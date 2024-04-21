package aor.paj.service;


import aor.paj.bean.*;
import aor.paj.dto.MessageDto;
import aor.paj.dto.TaskDto;
import aor.paj.entity.TaskEntity;
import aor.paj.service.status.Function;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Path("/messages")
public class MessageService {
    @EJB
    UserBean userBean;
    @EJB
    AppConfigurationsBean appConfigurationsBean;
    @EJB
    PermissionBean permissionBean;
    @EJB
    TaskBean taskBean;
    @EJB
    MessageBean messageBean;

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessagesBetweenUsers(@HeaderParam("token") String token, @PathParam("username")String username) {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                ArrayList<MessageDto> messageDtos=messageBean.getMessagesByTokenAndUsername(token,username);
                return Response.status(200).entity(messageDtos).build();
            }else return Response.status(401).entity("Session has expired").build();
        }return Response.status(403).entity("User not logged").build();
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createTask(@HeaderParam("token") String token,MessageDto messageDto) {
        if (userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                messageDto.setSendDate(LocalDateTime.now());
                messageDto.setSeen(false);
                if(messageBean.addMessage(messageDto)){
                    return Response.status(200).entity("A new message has been created").build();
                }
                else return Response.status(400).entity("Error creating message").build();
            }else return Response.status(401).entity("Session has expired").build();

        }return Response.status(403).entity("User permissions violated").build();
    }

    @PUT
    @Path("/{usernameSender}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createTask(@HeaderParam("token") String token, @PathParam("usernameSender")String usernameSender) {
        if (userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
               if(messageBean.setSeenToTrue(token,usernameSender)){
                   return Response.status(200).entity("All messages have been seen.").build();
               }else return  Response.status(400).entity("Error setting up the seen messages").build();
            }else return Response.status(401).entity("Session has expired").build();
        }return Response.status(403).entity("User permissions violated").build();
    }
}
