package aor.paj.websocket;

import aor.paj.bean.*;
import aor.paj.dto.MessageDto;
import aor.paj.dto.StatisticsDto;
import aor.paj.service.status.Function;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
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

    public boolean send(@PathParam("token")String token, @PathParam("username") String username, MessageDto messageDto) throws IOException {

        WebSocketSessionInfo webSocketSessionInfo = sessions.get(token);
        if (webSocketSessionInfo != null) {
            if (webSocketSessionInfo.getUsername().equals(username)) {
                try {
                    messageDto.setSeen(true);
                    webSocketSessionInfo.getSession().getBasicRemote().sendText("getMessages: " + messageDto.toString());
                    System.out.println("sending.......... ");
                    return true;
                } catch (IOException e) {
                    System.out.println("Something went wrong!");
                    return false;
                }
            } else return false;
        }else return false;
    }

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

    public boolean sendUnseenMessagesNumber(@PathParam("token")String token,@PathParam("username")String username){

        WebSocketSessionInfo webSocketSessionInfo = sessions.get(token);
        if (webSocketSessionInfo != null) {
            if (webSocketSessionInfo.getUsername().equals(username)) {
                try {
                    webSocketSessionInfo.getSession().getBasicRemote().sendText("Messages number: "+messageBean.getMessagesNumberByToken(token));
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
    public void toDoOnMessage(String msg){
        System.out.println("A new message is received: "+ msg);
    }
}
