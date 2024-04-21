package aor.paj.bean;

import aor.paj.dao.MessageDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.MessageDto;
import aor.paj.dto.User;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import javax.persistence.PersistenceException;
import java.util.ArrayList;

@Stateless
public class MessageBean {

    @EJB
    UserDao userDao;
    @EJB
    MessageDao messageDao;


    public MessageDto convertMessageEntityToMessageDto(MessageEntity messageEntity){
        return new MessageDto(messageEntity.getText(),messageEntity.getSendDate(), messageEntity.isSeen(),messageEntity.getSender().getFirstName(),messageEntity.getRecipient().getFirstName(),messageEntity.getSender().getUsername(),messageEntity.getRecipient().getUsername(), messageEntity.getSender().getPhotoURL());
    }
    public ArrayList<MessageDto> getMessagesByTokenAndUsername(String token, String username){
        UserEntity presentUser=userDao.findUserByToken(token);
        UserEntity otherUser=userDao.findUserByUsername(username);

        if(presentUser!=null && otherUser!=null){
            ArrayList<MessageEntity> allMessagesBetweenTwoUsers=messageDao.getMessagesBySenderAndRecipient(presentUser,otherUser);
            ArrayList<MessageDto> allMessagesDtos=new ArrayList<>();
            for (MessageEntity messageEntity:allMessagesBetweenTwoUsers){
                allMessagesDtos.add(convertMessageEntityToMessageDto(messageEntity));
            }
            return allMessagesDtos;
        }
        else return null;
    }

    public boolean addMessage(MessageDto messageDto){
        try {
            MessageEntity messageEntity = new MessageEntity(messageDto.getText(), messageDto.getSendDate(), messageDto.isSeen(),
                    userDao.findUserByUsername(messageDto.getSenderUsername()), userDao.findUserByUsername(messageDto.getRecipientUsername()));

            messageDao.persist(messageEntity);
            return true;
        }catch (PersistenceException e){
            return false;
        }
        catch (jakarta.ejb.EJBTransactionRolledbackException e){
            return false;
        }
    }

    public boolean setSeenToTrue(String token, String username){
        UserEntity sender=userDao.findUserByUsername(username);
        UserEntity recipient=userDao.findUserByToken(token);

        if(sender!=null && recipient!=null){
            if(messageDao.setMessagesSeenTotTrue(sender,recipient)){
                return true;
            }
            else return false;
        } else return false;
    }

}
