package aor.paj.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name="messages")
@NamedQuery(name = "Message.getMessagesByRecipientAndSender", query = "SELECT m FROM MessageEntity m WHERE (m.sender = :sender AND m.recipient=:recipient) OR( m.recipient=:sender AND m.sender=:recipient) ORDER BY m.sendDate")
@NamedQuery(name="Message.setSeenToTrue",query="UPDATE MessageEntity m SET m.seen = true " + "WHERE m.sender=:sender AND m.recipient=:recipient AND m.seen = false")
@NamedQuery(name="Message.getMessageSeenFalse",query="SELECT m FROM MessageEntity m WHERE m.sender=:sender AND m.recipient=:recipient AND m.seen = false")
@NamedQuery(name="Message.getMessagesNumberByRecipient", query="SELECT COUNT(m) FROM MessageEntity m WHERE m.recipient=:recipient AND m.seen=false")
@NamedQuery(name="Message.getMessagesSeenFalseNumber",query="SELECT COUNT(m) FROM MessageEntity m WHERE m.sender=:sender AND m.recipient=:recipient AND m.seen = false")
@NamedQuery(name="Message.getMessagesSeenFalseByRecipient",query="SELECT m FROM MessageEntity m WHERE  m.recipient=:recipient AND m.seen = false ORDER BY m.sendDate DESC")
@NamedQuery(name="Message.setSeenToTrueByRecipient",query="UPDATE MessageEntity m SET m.seen = true " + "WHERE  m.recipient=:recipient AND m.seen = false")
@NamedQuery(name="Message.getMessagesByRecipient",query="SELECT m FROM MessageEntity m WHERE  m.recipient=:recipient AND m.seen = true ORDER BY m.sendDate DESC")
@NamedQuery(name="Message.deleteMessageById", query="DELETE FROM MessageEntity m WHERE m.id=:id")
@NamedQuery(name="Message.getMessagesByUser",query="SELECT m FROM MessageEntity m WHERE m.sender=:user OR m.recipient=:user")
@NamedQuery(name="Message.getSendersSeenFalseByRecipient",query="SELECT DISTINCT m.sender FROM MessageEntity m WHERE m.seen = false AND m.recipient=:recipient")

public class MessageEntity implements Serializable {

    private static final long longSerialVersionID=1L;

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    @Column(name="id", nullable = false,unique = true,updatable = false)
    private long id;
    @Column(name="text", nullable = true,unique =false,updatable = true)
    private String text;
    @Column (name="send_date", nullable = false, unique = false,updatable = false)
    private LocalDateTime sendDate;
    @Column(name="seen", nullable = false,unique = false,updatable = true)
    private boolean seen;
    @NotNull
    @ManyToOne
    private UserEntity sender;
    @NotNull
    @ManyToOne
    private UserEntity recipient;

    public MessageEntity() {}

    public MessageEntity(String text, LocalDateTime sendDate, boolean seen, UserEntity sender, UserEntity recipient) {
        this.text = text;
        this.sendDate = sendDate;
        this.seen = seen;
        this.sender = sender;
        this.recipient = recipient;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public UserEntity getSender() {
        return sender;
    }

    public void setSender(UserEntity sender) {
        this.sender = sender;
    }

    public UserEntity getRecipient() {
        return recipient;
    }

    public void setRecipient(UserEntity recipient) {
        this.recipient = recipient;
    }
}
