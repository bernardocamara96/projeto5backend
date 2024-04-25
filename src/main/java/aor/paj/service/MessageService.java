package aor.paj.service;


import aor.paj.bean.*;
import aor.paj.dto.MessageDto;
import aor.paj.dto.TaskDto;
import aor.paj.entity.TaskEntity;
import aor.paj.service.status.Function;
import aor.paj.websocket.MessageWebSocket;
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
    public Response createMessage(@HeaderParam("token") String token,MessageDto messageDto) throws IOException {
        if (userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                messageDto.setSendDate(LocalDateTime.now());
                messageDto.setSeen(false);
                try {
                    if (messageBean.sendWebSocketMessage(token, messageDto)) {
                        if (messageBean.addMessage(messageDto)) {
                            return Response.status(200).entity(true).build();
                        }
                    }
                    if(messageBean.addMessage(messageDto)){
                        messageBean.sendUnseenMessages(messageDto.getRecipientUsername());
                        return Response.status(200).entity(false).build();
                    }else return Response.status(400).entity("Eroor creating message").build();
                }catch(IOException e){return Response.status(400).entity("Error creating message").build();}
            }else return Response.status(401).entity("Session has expired").build();
        }return Response.status(403).entity("User permissions violated").build();
    }

    @PUT
    @Path("/saw/{usernameSender}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setMessagesSennToTrue(@HeaderParam("token") String token, @PathParam("usernameSender")String usernameSender) throws IOException {
        if (userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
               if(messageBean.setSeenToTrue(token,usernameSender)){
                   return Response.status(200).entity("All messages have been seen.").build();
               }else return  Response.status(400).entity("Error setting up the seen messages").build();
            }else return Response.status(401).entity("Session has expired").build();
        }return Response.status(403).entity("User permissions violated").build();
    }

    @PUT
    @Path("/saw")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setMessagesSennToTrueByRecipient(@HeaderParam("token") String token) throws IOException {
        if (userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                if(messageBean.setSeenToTrueByRecipient(token)){
                    return Response.status(200).entity("All messages have been seen.").build();
                }else return  Response.status(400).entity("Error setting up the seen messages").build();
            }else return Response.status(401).entity("Session has expired").build();
        }return Response.status(403).entity("User permissions violated").build();
    }

    @GET
    @Path("/number")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessagesNumber(@HeaderParam("token") String token) {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                return Response.status(200).entity(messageBean.getMessagesNumberByToken(token)).build();
            }else return Response.status(401).entity("Session has expired").build();
        }return Response.status(403).entity("User not logged").build();
    }

    @GET
    @Path("/notifications")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNotifications(@HeaderParam("token") String token) {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                return Response.status(200).entity(messageBean.getNotifications(token)).build();
            }else return Response.status(401).entity("Session has expired").build();
        }return Response.status(403).entity("User not logged").build();
    }
}
