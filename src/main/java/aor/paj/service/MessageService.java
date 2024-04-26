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
import org.apache.logging.log4j.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
    private static final Logger logger=LogManager.getLogger(MessageService.class);

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessagesBetweenUsers(@HeaderParam("token") String token, @PathParam("username")String username) throws UnknownHostException {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                ArrayList<MessageDto> messageDtos=messageBean.getMessagesByTokenAndUsername(token,username);
                logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" requested the messages from "+username);
                return Response.status(200).entity(messageDtos).build();
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when requested the messages from "+username);
                return Response.status(401).entity("Session has expired").build();
            }
        }
        logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when requested the messages from "+username);
        return Response.status(403).entity("User not logged").build();
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
                            logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+ userBean.findUsernameByToken(token)+" messaged "+messageDto.getRecipientUsername());
                            return Response.status(200).entity(true).build();
                        }
                    }
                    if(messageBean.addMessage(messageDto)){
                        messageBean.sendUnseenMessages(messageDto.getRecipientUsername());
                        logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+ userBean.findUsernameByToken(token)+" messaged "+messageDto.getRecipientUsername());
                        return Response.status(200).entity(false).build();
                    }else {
                        logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+ userBean.findUsernameByToken(token)+" had an error when messaged "+messageDto.getRecipientUsername());
                        return Response.status(400).entity("Error creating message").build();
                    }
                }catch(IOException e){
                    logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+ userBean.findUsernameByToken(token)+" had an error when messaged "+messageDto.getRecipientUsername());
                    return Response.status(400).entity("Error creating message").build();
                }
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when messaged "+messageDto.getRecipientUsername());
                return Response.status(401).entity("Session has expired").build();
            }
        }
        logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when messaged "+messageDto.getRecipientUsername());
        return Response.status(403).entity("User permissions violated").build();
    }

    @PUT
    @Path("/saw/{usernameSender}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setMessagesSennToTrue(@HeaderParam("token") String token, @PathParam("usernameSender")String usernameSender) throws IOException {
        if (userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
               if(messageBean.setSeenToTrue(token,usernameSender)){
                   logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+ userBean.findUsernameByToken(token)+" have seen all messages from "+usernameSender);
                   return Response.status(200).entity("All messages have been seen.").build();
               }else {
                   logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+ userBean.findUsernameByToken(token)+" had an error when saw all messages from "+usernameSender);
                   return Response.status(400).entity("Error setting up the seen messages").build();
               }
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when tried to see all messages from "+usernameSender);
                return Response.status(401).entity("Session has expired").build();
            }
        }
        logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when tried to see all messages from "+usernameSender);
        return Response.status(403).entity("User permissions violated").build();
    }

    @PUT
    @Path("/saw")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setMessagesSennToTrueByRecipient(@HeaderParam("token") String token) throws IOException {
        if (userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                if(messageBean.setSeenToTrueByRecipient(token)){
                    logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+ userBean.findUsernameByToken(token)+" saw all messages that were send to him ");
                    return Response.status(200).entity("All messages have been seen.").build();
                }else {
                    logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+ userBean.findUsernameByToken(token)+" had an error when saw all messages send to him ");
                    return Response.status(400).entity("Error setting up the seen messages").build();
                }
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+ userBean.findUsernameByToken(token)+" had session expired when saw all messages send to him ");
                return Response.status(401).entity("Session has expired").build();
            }
        }
        logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+ userBean.findUsernameByToken(token)+" had access denied when saw all messages send to him ");
        return Response.status(403).entity("User permissions violated").build();
    }

    @GET
    @Path("/number")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessagesNumber(@HeaderParam("token") String token) throws UnknownHostException {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+ userBean.findUsernameByToken(token)+" requested the number of unseen messages he got");
                return Response.status(200).entity(messageBean.getMessagesNumberByToken(token)).build();
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when requested the number of unseen messages he got");
                return Response.status(401).entity("Session has expired").build();
            }
        }
        logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when requested the number of unseen messages he got");
        return Response.status(403).entity("User not logged").build();
    }

    @GET
    @Path("/notifications")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNotifications(@HeaderParam("token") String token) throws UnknownHostException {
        if(userBean.tokenValidator(token)){
            if(appConfigurationsBean.validateTimeout(token)) {
                logger.info(InetAddress.getLocalHost().getHostAddress()+" requested the notifications he got");
                return Response.status(200).entity(messageBean.getNotifications(token)).build();
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when requested the notifications he got");
                return Response.status(401).entity("Session has expired").build();
            }
        }
        logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when requested the notifications he got");
        return Response.status(403).entity("User not logged").build();
    }
}
