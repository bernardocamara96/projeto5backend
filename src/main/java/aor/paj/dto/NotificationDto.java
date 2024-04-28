package aor.paj.dto;


import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.Duration;

@XmlRootElement
public class NotificationDto {

    @XmlElement
    private String senderFirstName;
    @XmlElement
    private String senderUsername;
   
    @XmlElement
    private String text;
    @XmlElement
    private String photoUrl;
    @XmlElement
    private Duration timestamp;
    @XmlElement
    private boolean seen;

    public NotificationDto(String senderFirstName, String senderUsername,String text,String photoUrl, Duration timestamp,boolean seen) {
        this.senderFirstName = senderFirstName;
        this.senderUsername = senderUsername;
        this.text=text;
        this.photoUrl=photoUrl;
        this.timestamp=timestamp;
        this.seen=seen;
    }

    public String getSenderFirstName() {
        return senderFirstName;
    }

    public void setSenderFirstName(String senderFirstName) {
        this.senderFirstName = senderFirstName;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Duration getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Duration timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    @Override
    public String toString() {
        return "{" +
                "\"username\": \"" + senderUsername + "\"," +
                "\"firstName\": \"" + senderFirstName + "\"" +
                "\"text\": \"" + text + "\"" +
                "\"photoUrl\": \"" + photoUrl + "\"" +
                "\"timestamp\": \"" + timestamp + "\"" +
                "\"seen\": \"" + seen + "\"" +
                "}";
    }
}
