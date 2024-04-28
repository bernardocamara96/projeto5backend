package aor.paj.websocket;

import aor.paj.bean.AppConfigurationsBean;
import aor.paj.bean.PermissionBean;
import aor.paj.bean.StatisticsBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.StatisticsDto;
import aor.paj.service.status.Function;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Singleton
@ServerEndpoint("/websocket/dashboard/{token}")
public class DashboardWebSocket {
    HashMap<String, Session> sessions = new HashMap<String, Session>();
    @Inject
    StatisticsBean statisticsBean;
    @Inject
    UserBean userBean;
    @Inject
    AppConfigurationsBean appConfigurationsBean;
    @Inject
    PermissionBean permissionBean;

    /**
     * Sends statistics to all connected WebSocket sessions.
    */
    public void send() throws IOException {
        ArrayList<String> tokens=userBean.getAllTokens();

        for (String token:tokens) {
            Session session = sessions.get(token);
            if (session != null) {
                StatisticsDto statisticsDto = new StatisticsDto();
                statisticsBean.setStatistics(statisticsDto);
                try {
                    session.getBasicRemote().sendText(statisticsDto.toString());
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
                        if (permissionBean.getPermission(token, Function.GET_STATISTICS)) {
                            sessions.put(token, session);
                            System.out.println("A new Dashboard WebSocket session is opened for client with token: "+ token);
                        } else session.close();
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