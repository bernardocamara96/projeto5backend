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
    @OnMessage
    public void toDoOnMessage(String message) throws UnknownHostException {

        GsonBuilder gsonBuilder = new GsonBuilder();

        // Register the LocalDateTimeAdapter with GsonBuilder
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());

        // Create a Gson instance
        Gson gson = gsonBuilder.create();
        MessageDto messageDto=gson.fromJson(message, MessageDto.class);
        UserEntity recipient=userBean.getUserByUsername(messageDto.getRecipientUsername());

        String recipientToken=null;
        if(recipient!=null) {
            recipientToken = recipient.getToken();

            UserEntity sender = userBean.getUserByUsername(messageDto.getSenderUsername());

            messageDto.setSenderPhoto(sender.getPhotoURL());
            messageDto.setSendDate(LocalDateTime.now());
            messageDto.setSenderFirstName(sender.getFirstName());
            messageDto.setRecipientFirstName(recipient.getFirstName());

            if (!recipientToken.isEmpty()) {

                WebSocketSessionInfo webSocketSessionInfo = sessions.get(recipientToken);
                if (webSocketSessionInfo != null) {
                    if (webSocketSessionInfo.getUsername().equals(messageDto.getSenderUsername())) {
                        try {
                            messageDto.setSeen(true);
                            addMessageMethod(messageDto,sender,recipient);
                            sendSeenMessages(sender.getToken(), recipient.getUsername());
                            webSocketSessionInfo.getSession().getBasicRemote().sendText("getMessages: " + messageDto.toString());
                            System.out.println("sending.......... ");
                        } catch (IOException e) {
                            System.out.println("Something went wrong!");
                        }
                    }else {
                        messageDto.setSeen(false);
                        addMessageMethod(messageDto,sender,recipient);
                        notificationsWebSocket.sendUnseenMessagesNumber(recipientToken);
                    }
                }else {
                    messageDto.setSeen(false);
                    addMessageMethod(messageDto,sender,recipient);
                    notificationsWebSocket.sendUnseenMessagesNumber(recipientToken);
                }
            } else {
                messageDto.setSeen(false);
                addMessageMethod(messageDto,sender,recipient);
            }
        }
    }

    private void addMessageMethod(MessageDto messageDto, UserEntity sender, UserEntity recipient) throws UnknownHostException {

        messageBean.addMessage(messageDto);
        logger.info(InetAddress.getLocalHost().getHostAddress() + "  " + sender + " messaged " + recipient);
    }
}
