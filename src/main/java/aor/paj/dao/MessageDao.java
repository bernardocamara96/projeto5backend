package aor.paj.dao;

import aor.paj.dto.User;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

import java.util.ArrayList;

@Stateless
public class MessageDao extends AbstractDao<MessageEntity>{

    public MessageDao() {
        super(MessageEntity.class);
    }

    public ArrayList<MessageEntity> getMessagesBySenderAndRecipient(UserEntity sender, UserEntity recipient) {
        try {
            return (ArrayList<MessageEntity>) em.createNamedQuery("Message.getMessagesByRecipientAndSender").setParameter("sender", sender)
                    .setParameter("recipient",recipient)
                    .getResultList();

        } catch (NoResultException e) {
            return null;
        }
    }

    public boolean setMessagesSeenTotTrue(UserEntity sender,UserEntity recipient){
        try{
            em.createNamedQuery("Message.setSeenToTrue").setParameter("sender",sender).setParameter("recipient",recipient).executeUpdate();
            return true;
        }catch (Exception e) {
            return false;
        }
    }

}
