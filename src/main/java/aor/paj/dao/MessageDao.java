package aor.paj.dao;

import aor.paj.dto.User;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

import java.time.LocalDateTime;
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

    public boolean setMessagesSeenToTrueByRecipient(UserEntity recipient){
        try{
            em.createNamedQuery("Message.setSeenToTrueByRecipient").setParameter("recipient",recipient).executeUpdate();
            return true;
        }catch (Exception e) {
            return false;
        }
    }


    public ArrayList<MessageEntity> getMessagesSeenFalse(UserEntity sender,UserEntity recipient){
        try{
           return (ArrayList<MessageEntity>) em.createNamedQuery("Message.getMessageSeenFalse").setParameter("sender",sender).setParameter("recipient",recipient).getResultList();

        }catch (NoResultException e) {
            return null;
        }
    }

    public ArrayList<MessageEntity> getMessagesSeenTrueByRecipient(UserEntity recipient){
        try{
            return (ArrayList<MessageEntity>) em.createNamedQuery("Message.getMessagesByRecipient").setParameter("recipient",recipient).getResultList();

        }catch (NoResultException e) {
            return null;
        }
    }


    public int getMessagesNumberByRecipient(UserEntity recipient) {
        try {
            return ((Number) em.createNamedQuery("Message.getMessagesNumberByRecipient")
                    .setParameter("recipient",recipient)
                    .getSingleResult()).intValue();

        } catch (NoResultException e) {
            return 0;
        }
    }

    public int getMessagesSeenFalseNumber(UserEntity sender,UserEntity recipient){
        try{
            return ((Number) em.createNamedQuery("Message.getMessagesSeenFalseNumber").setParameter("sender",sender).setParameter("recipient",recipient).getSingleResult()).intValue();

        }catch (NoResultException e) {
            return 0;
        }
    }

    public ArrayList<MessageEntity> getMessagesSeenFalseByRecipient(UserEntity recipient){
        try{
            return (ArrayList<MessageEntity>) em.createNamedQuery("Message.getMessagesSeenFalseByRecipient").setParameter("recipient",recipient).getResultList();

        }catch (NoResultException e) {
            return null;
        }
    }

    public ArrayList<MessageEntity> getMessagesByUser(UserEntity user){
        try{
            return (ArrayList<MessageEntity>) em.createNamedQuery("Message.getMessagesByUser").setParameter("user",user).getResultList();

        }catch (NoResultException e) {
            return null;
        }
    }

    public void deleteMessageById(long id){
        try{
            em.createNamedQuery("Message.deleteMessageById").setParameter("id",id).executeUpdate();

        }catch (Exception e){
        }
    }


}
