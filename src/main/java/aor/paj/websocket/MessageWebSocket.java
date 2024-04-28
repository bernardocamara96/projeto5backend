package aor.paj.websocket;

import aor.paj.bean.*;
import aor.paj.dto.MessageDto;
import aor.paj.dto.StatisticsDto;
import aor.paj.dto.User;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.UserEntity;
import aor.paj.service.MessageService;
import aor.paj.service.status.Function;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import util.LocalDateTimeAdapter;
import org.apache.logging.log4j.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;


@Singleton
@ServerEndpoint("/websocket/message/{token}/{username}")
public class MessageWebSocket {


    HashMap<String, WebSocketSessionInfo> sessions = new HashMap<String, WebSocketSessionInfo>();
    @Inject
    UserBean userBean;
    @Inject
    AppConfigurationsBean appConfigurationsBean;
    @Inject
    MessageBean messageBean;
    @Inject
    NotificationsWebSocket notificationsWebSocket;
    private static final Logger logger=LogManager.getLogger(MessageWebSocket.class);


    /**
     * Sends the information that all the messages were seen to the WebSocket session associated with the given token and username.
    */
 public boolean sendSeenMessages(@PathParam("token")String token, @PathParam("username") String username){

        WebSocketSessionInfo webSocketSessionInfo = sessions.get(token);
        if (webSocketSessionInfo != null) {
            if (webSocketSessionInfo.getUsername().equals(username)) {
                try {
                    System.out.println(  messageBean.getMessagesSeenFalse(token,username));
                    webSocketSessionInfo.getSession().getBasicRemote().sendText("All messages were seen: ");
                    System.out.println("sending.......... ");
                    return true;
                } catch (IOException e) {
                    System.out.println("Something went wrong!");
                    return false;
                }
            } else return false;
        }else return false;
    }



    @OnOpen
    public void toDoOnOpen(Session session, @PathParam("token") String token, @PathParam("username") String username){
        try {
            if (userBean.tokenValidator(token)) {
                if (appConfigurationsBean.validateTimeout(token)) {
                        sessions.put(token, new WebSocketSessionInfo(username,session));
                        System.out.println("A new Message WebSocket session is opened for client with token: "+ token+" and username "+username);
                } else session.close();
            } else session.close();
        }catch(IOException e){
            System.out.println("Error opening websocket");
        }
    }
    @OnClose
    public void toDoOnClose(Session session ,CloseReason reason,@PathParam("token") String token){
        System.out.println("Websocket session is closed with CloseCode: "+
                reason.getCloseCode() + ": "+reason.getReasonPhrase());
        sessions.values().removeIf(sessionInfo -> sessionInfo.getSession().equals(session));
    }


/**
 * Handles the incoming message from the WebSocket.
 * Parses the incoming message into a MessageDto object.
 * Retrieves the recipient user and their token.
 * If the recipient is connected via WebSocket, marks the message as seen,
 * adds the message to the database, and sends it to the recipient's WebSocket.
 * If the recipient is not connected, adds the message to the database and sends unseen message count.
*/
 @OnMessage
    public void toDoOnMessage(String message) throws IOException {

        GsonBuilder gsonBuilder = new GsonBuilder();

        // Register the LocalDateTimeAdapter with GsonBuilder
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());

        // Create a Gson instance
        Gson gson = gsonBuilder.create();
        MessageDto messageDto=gson.fromJson(message, MessageDto.class);

        String senderToken= userBean.findTokenByUsername(messageDto.getSenderUsername());
        String recipientToken=userBean.findTokenByUsername(messageDto.getRecipientUsername());

        WebSocketSessionInfo webSocketSessionInfoSender=sessions.get(senderToken);

        if(appConfigurationsBean.validateTimeout(senderToken)) {
            if (recipientToken != null) {

               messageBean.auxiliarMethodMessageDto(messageDto);

                if (!recipientToken.isEmpty()) {
                    WebSocketSessionInfo webSocketSessionInfo = sessions.get(recipientToken);
                    if (webSocketSessionInfo != null) {
                        if (webSocketSessionInfo.getUsername().equals(messageDto.getSenderUsername())) {
                            if(appConfigurationsBean.validateTimeout(recipientToken)) {
                                try {
                                    messageDto.setSeen(true);
                                    addMessageMethod(messageDto, messageDto.getSenderFirstName(), messageDto.getRecipientFirstName());
                                    sendSeenMessages(senderToken,messageDto.getRecipientUsername());
                                    webSocketSessionInfo.getSession().getBasicRemote().sendText("getMessages: " + messageDto.toString());
                                    System.out.println("sending.......... ");
                                } catch (IOException e) {
                                    System.out.println("Something went wrong!");
                                }
                            }else {
                                messageDto.setSeen(false);
                                addMessageMethod(messageDto,messageDto.getSenderFirstName(),messageDto.getRecipientFirstName());
                                webSocketSessionInfo.getSession().getBasicRemote().sendText("Token has expired");
                            }
                        } else {
                            messageDto.setSeen(false);
                            addMessageMethod(messageDto, messageDto.getSenderFirstName(), messageDto.getRecipientFirstName());
                            notificationsWebSocket.sendUnseenMessagesNumber(recipientToken);
                        }
                    } else {
                        messageDto.setSeen(false);
                        addMessageMethod(messageDto, messageDto.getSenderFirstName(), messageDto.getRecipientFirstName());
                        notificationsWebSocket.sendUnseenMessagesNumber(recipientToken);
                    }
                } else {
                    messageDto.setSeen(false);
                    addMessageMethod(messageDto, messageDto.getSenderFirstName(), messageDto.getRecipientFirstName());
                }
            }
        }else {
            webSocketSessionInfoSender.getSession().getBasicRemote().sendText("Token has expired");
        }
    }

    private void addMessageMethod(MessageDto messageDto, String senderName, String recipientName) throws UnknownHostException {

        messageBean.addMessage(messageDto);
        logger.info(InetAddress.getLocalHost().getHostAddress() + "  " + senderName + " messaged " + recipientName);
    }
}
