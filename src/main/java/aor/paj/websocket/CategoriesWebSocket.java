package aor.paj.websocket;

import aor.paj.bean.*;
import aor.paj.dto.CategoryStatsDto;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@Singleton
@ServerEndpoint("/websocket/categories/{token}")
public class CategoriesWebSocket {
    HashMap<String, Session> sessions = new HashMap<String, Session>();

    @Inject
    UserBean userBean;
    @Inject
    AppConfigurationsBean appConfigurationsBean;
    @Inject
    CategoryBean categoryBean;

    /**
     * Sends category statistics to all active sessions.
     */
    public void send() throws IOException {
        ArrayList<String> tokens=userBean.getAllTokens();

        for (String token:tokens) {
            Session session = sessions.get(token);
            if (session != null) {
                try {
                    ArrayList<CategoryStatsDto>categoryStatsDtos=categoryBean.ordenedCategoriesList();
                    session.getBasicRemote().sendText(categoryStatsDtos.toString());

                    System.out.println("sending.......... ");
                } catch (IOException e) {
                    System.out.println("Something went wrong!");
                }
            }
        }
    }

    @OnOpen
    public void toDoOnOpen(Session session, @PathParam("token") String token){
        try {
            if (userBean.tokenValidator(token)) {
                if (appConfigurationsBean.validateTimeout(token)) {
                        sessions.put(token, session);
                        System.out.println("A new Categories WebSocket session is opened for client with token: "+ token);
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
    public void toDoOnMessage(String msg){
        System.out.println("A new message is received: "+ msg);

    }

}