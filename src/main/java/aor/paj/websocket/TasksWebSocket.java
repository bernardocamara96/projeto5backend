package aor.paj.websocket;

import aor.paj.bean.*;
import aor.paj.dto.StatisticsDto;
import aor.paj.dto.TaskDto;
import aor.paj.dto.TaskStatusDto;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


@Singleton
@ServerEndpoint("/websocket/tasks/{token}")
public class TasksWebSocket {
    HashMap<String, Session> sessions = new HashMap<String, Session>();

    @Inject
    UserBean userBean;
    @Inject
    AppConfigurationsBean appConfigurationsBean;
    @Inject
    PermissionBean permissionBean;



    public void sendEditTask(TaskDto taskDto) throws IOException {
        ArrayList<String> tokens=userBean.getAllTokens();

        for (String token:tokens) {
            Session session = sessions.get(token);
            if (session != null) {
                try {
                    session.getBasicRemote().sendText("taskEdit: "+taskDto.toString());
                    System.out.println("sending.......... ");
                } catch (IOException e) {
                    System.out.println("Something went wrong!");
                }
            }
        }
    }

    public void sendDeleteTempTask(TaskDto taskDto) throws IOException {
        ArrayList<String> tokens=userBean.getAllTokens();

        for (String token:tokens) {
            Session session = sessions.get(token);
            if (session != null) {
                try {
                    session.getBasicRemote().sendText("taskTempDelete: "+taskDto.toString());
                    System.out.println("sending.......... ");
                } catch (IOException e) {
                    System.out.println("Something went wrong!");
                }
            }
        }
    }

    public void sendRecycleTask(TaskDto taskDto) throws IOException {
        ArrayList<String> tokens=userBean.getAllTokens();

        for (String token:tokens) {
            Session session = sessions.get(token);
            if (session != null) {
                try {
                    session.getBasicRemote().sendText("taskRecycle: "+taskDto.toString());
                    System.out.println("sending.......... ");
                } catch (IOException e) {
                    System.out.println("Something went wrong!");
                }
            }
        }
    }

    public void sendPermDeleteTask(TaskDto taskDto) throws IOException {
        ArrayList<String> tokens=userBean.getAllTokens();

        for (String token:tokens) {
            Session session = sessions.get(token);
            if (session != null) {
                try {
                    session.getBasicRemote().sendText("taskPermDelete: "+taskDto.getId());
                    System.out.println("sending.......... ");
                } catch (IOException e) {
                    System.out.println("Something went wrong!");
                }
            }
        }
    }


    public void sendAllTempDeleteTasks(ArrayList<TaskDto> taskDtos) throws IOException {
        ArrayList<String> tokens=userBean.getAllTokens();

        for (String token:tokens) {
            Session session = sessions.get(token);
            if (session != null) {
                try {
                    ArrayList<String> taskDtosString= new ArrayList<>();
                    for(TaskDto taskDto:taskDtos){
                        taskDtosString.add(taskDto.toString());
                    }
                    session.getBasicRemote().sendText("allTasksTempDelete: "+taskDtosString);
                    System.out.println("sending.......... ");
                } catch (IOException e) {
                    System.out.println("Something went wrong!");
                }
            }
        }
    }


    public void sendStatusTask(TaskStatusDto taskStatusDto) throws IOException {
        ArrayList<String> tokens=userBean.getAllTokens();

        for (String token:tokens) {
            Session session = sessions.get(token);
            if (session != null) {
                try {
                    session.getBasicRemote().sendText("taskStatus: "+taskStatusDto.toString());
                    System.out.println("sending.......... ");
                } catch (IOException e) {
                    System.out.println("Something went wrong!");
                }
            }
        }
    }



    public void sendNewTask(TaskDto taskDto) throws IOException {
        ArrayList<String> tokens=userBean.getAllTokens();

        for (String token:tokens) {
            Session session = sessions.get(token);
            if (session != null) {
                try {

                    session.getBasicRemote().sendText("newTask: "+taskDto.toString());
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
                        System.out.println("A new Tasks WebSocket session is opened for client with token: "+ token);

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