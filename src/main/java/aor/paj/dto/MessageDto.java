package aor.paj.dto;

import aor.paj.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.LocalDateTime;

@XmlRootElement
public class MessageDto {

    @XmlElement
    private String text;
    @XmlElement
    private LocalDateTime sendDate;
    @XmlElement
    private boolean seen;
    @XmlElement
    private String senderFirstName;
    @XmlElement
    private String recipientFirstName;
    @XmlElement
    private String recipientUsername;
    @XmlElement
    private String senderUsername;
    @XmlElement
    private String senderPhoto;



    public MessageDto() {
    }

    public MessageDto(String text, LocalDateTime sendDate, boolean seen, String senderFirstName, String recipientFirstName, String senderUsername, String recipientUsername, String senderPhoto) {
        this.text = text;
        this.sendDate = sendDate;
        this.seen = seen;
        this.senderFirstName = senderFirstName;
        this.recipientFirstName = recipientFirstName;
        this.recipientUsername=recipientUsername;
        this.senderUsername=senderUsername;
        this.senderPhoto=senderPhoto;

    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getSendDate() {
        return sendDate;
    }

    public void setSendDate(LocalDateTime sendDate) {
        this.sendDate = sendDate;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getSenderFirstName() {
        return senderFirstName;
    }

    public void setSenderFirstName(String senderFirstName) {
        this.senderFirstName = senderFirstName;
    }

    public String getRecipientFirstName() {
        return recipientFirstName;
    }

    public void setRecipientFirstName(String recipientFirstName) {
        this.recipientFirstName = recipientFirstName;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getSenderPhoto() {
        return senderPhoto;
    }

    public void setSenderPhoto(String senderPhoto) {
        this.senderPhoto = senderPhoto;
    }


}
