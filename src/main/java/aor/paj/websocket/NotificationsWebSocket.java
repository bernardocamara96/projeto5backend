package aor.paj.websocket;


import aor.paj.bean.AppConfigurationsBean;
import aor.paj.bean.MessageBean;
import aor.paj.bean.UserBean;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
@Singleton
@ServerEndpoint("/websocket/notifications/{token}")
public class NotificationsWebSocket{
    HashMap<String, Session> sessions = new HashMap<String, Session>();
    @Inject
    UserBean userBean;
    @Inject
    MessageBean messageBean;
    @Inject
    AppConfigurationsBean appConfigurationsBean;

    /**
     * Sends the number of unseen messages to the WebSocket session associated with the given token.
    */
 public boolean sendUnseenMessagesNumber(@PathParam("token")String token){

        Session session = sessions.get(token);
        if (session!= null) {

                try {
                    session.getBasicRemote().sendText("Messages number: "+messageBean.getMessagesNumberByToken(token));
                    System.out.println("sending.......... ");
                    return true;
                } catch (IOException e) {
                    System.out.println("Something went wrong!");
                    return false;
                }

        }else return false;
    }

    @OnOpen
    public void toDoOnOpen(Session session, @PathParam("token") String token){
        try {
            if (userBean.tokenValidator(token)) {
                if (appConfigurationsBean.validateTimeout(token)) {
                    sessions.put(token, session);
                } else session.close();
            } else session.close();
        }catch(IOException e){
            System.out.println("Error opening websocket");
        }
    }
    @OnClose
    public void toDoOnClose(Session session, CloseReason reason){
        System.out.println("Websocket session is closed with CloseCode: "+
                reason.getCloseCode() + ": "+reason.getReasonPhrase());
        sessions.values().removeIf(s -> s.equals(session));
    }
    @OnMessage
    public void toDoOnMessage(Session session, String msg){
        System.out.println("A new message is received: "+ msg);
        try {
            session.getBasicRemote().sendText("ack");
        } catch (IOException e) {
            System.out.println("Something went wrong!");
        }
    }

}