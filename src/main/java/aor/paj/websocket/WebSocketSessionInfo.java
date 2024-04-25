package aor.paj.websocket;

import jakarta.websocket.Session;

public class WebSocketSessionInfo {
    private String username;
    private Session session;


    public WebSocketSessionInfo( String username, Session session) {
        this.username = username;
        this.session=session;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
