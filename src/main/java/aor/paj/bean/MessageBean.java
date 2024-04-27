package aor.paj.bean;

import aor.paj.dao.MessageDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.MessageDto;
import aor.paj.dto.NotificationDto;
import aor.paj.dto.User;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.UserEntity;
import aor.paj.websocket.MessageWebSocket;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import javax.persistence.PersistenceException;
import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Stateless
public class MessageBean implements Serializable {

    @EJB
    UserDao userDao;
    @EJB
    MessageDao messageDao;
    @Inject
    MessageWebSocket messageWebSocket;


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

    public boolean setSeenToTrue(String token, String username) throws IOException {
        UserEntity sender=userDao.findUserByUsername(username);
        UserEntity recipient=userDao.findUserByToken(token);

        if(sender!=null && recipient!=null){
            messageWebSocket.sendSeenMessages(sender.getToken(),recipient.getUsername());

            if(messageDao.setMessagesSeenTotTrue(sender,recipient)){
                return true;
            }
            else return false;
        } else return false;
    }

    public boolean setSeenToTrueByRecipient(String token) throws IOException {
        UserEntity recipient=userDao.findUserByToken(token);
        if(recipient!=null){
            if(messageDao.setMessagesSeenToTrueByRecipient(recipient)){
                return true;
            }else return false;
        } else return false;
    }

    public ArrayList<MessageDto> getMessagesSeenFalse(String token, String username){
        UserEntity recipient=userDao.findUserByUsername(username);
        UserEntity sender=userDao.findUserByToken(token);
        if(sender!=null && recipient!=null){
            ArrayList<MessageEntity> messageEntities=messageDao.getMessagesSeenFalse(sender,recipient);
            ArrayList<MessageDto> messageDtos=new ArrayList<>();
            for (MessageEntity messageEntity:messageEntities){
                messageDtos.add(convertMessageEntityToMessageDto(messageEntity));
            }
            return messageDtos;
        }
        return null;
    }

//    public boolean sendWebSocketMessage(String token,MessageDto messageDto ) throws IOException {
//        UserEntity userEntity=userDao.findUserByToken(token);
//        String senderUsername=userEntity.getUsername();
//        String recipientToken=userDao.findTokenByUsername(messageDto.getRecipientUsername());
//        try{
//            messageDto.setSenderPhoto(userEntity.getPhotoURL());
//            if(messageWebSocket.send(recipientToken,senderUsername,messageDto)) return true;
//            else return false;
//        } catch (IOException e){
//            return false;
//        }
//    }


   public int getMessagesNumberByToken(String token){
        UserEntity recipient=userDao.findUserByToken(token);
        return messageDao.getMessagesNumberByRecipient(recipient);
   }

//   public void sendUnseenMessages(String username){
//        UserEntity user=userDao.findUserByUsername(username);
//        messageWebSocket.sendUnseenMessagesNumber(user.getToken(),username);
//   }

   public ArrayList<NotificationDto> getNotifications(String token){
        int length=0;
       UserEntity recipient=userDao.findUserByToken(token);
        ArrayList<MessageEntity> messageEntities=messageDao.getMessagesSeenFalseByRecipient(recipient);
        ArrayList<NotificationDto> notificationDtos=new ArrayList<>();
        for(MessageEntity message:messageEntities){
            UserEntity sender=message.getSender();
            LocalDateTime sendDate=message.getSendDate();
            Duration duration=Duration.between(sendDate,LocalDateTime.now());
            notificationDtos.add(new NotificationDto(sender.getFirstName(), sender.getUsername(),message.getText(),sender.getPhotoURL(), duration,message.isSeen()));
        }
        ArrayList<MessageEntity> messageEntitiesSaw=messageDao.getMessagesSeenTrueByRecipient(recipient);
        if(messageEntitiesSaw.size()>5){
            length=5;
        }else {
            length=messageEntitiesSaw.size();
        }
        for(int i=0;i<length;i++){
            UserEntity sender=messageEntitiesSaw.get(i).getSender();
            LocalDateTime sendDate=messageEntitiesSaw.get(i).getSendDate();
            Duration duration=Duration.between(sendDate,LocalDateTime.now());
            notificationDtos.add(new NotificationDto(sender.getFirstName(),sender.getUsername(),messageEntitiesSaw.get(i).getText(),sender.getPhotoURL(),duration,messageEntitiesSaw.get(i).isSeen()));
        }
        return notificationDtos;
   }

   public void deleteMessagesByUser(String username){
        UserEntity user=userDao.findUserByUsername(username);
        ArrayList<MessageEntity> messageEntities=messageDao.getMessagesByUser(user);
        for (MessageEntity message:messageEntities) {
            messageDao.deleteMessageById(message.getId());
        }
   }





}
