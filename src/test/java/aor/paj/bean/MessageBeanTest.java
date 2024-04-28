package aor.paj.bean;

import aor.paj.dao.MessageDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.MessageDto;
import aor.paj.dto.NotificationDto;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MessageBeanTest {

    @InjectMocks
    MessageBean messageBean;
    @Mock
    MessageDao messageDao;
    @Mock
    UserDao userDao;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        UserEntity senderUserEntity=new UserEntity("senderTest","passSender","sendermail@mail.com","senderName","senderLastName","123123123","https://icons.veryicon.com/png/o/miscellaneous/yuanql/icon-admin.png","senderToken","developer",false,true);
        UserEntity recipientUserEntity=new UserEntity("recipientTest","passRecipient","recipientmail@mail.com","recipientName","recipientLastName","123123124","https://icons.veryicon.com/png/o/miscellaneous/yuanql/icon-admin.png","recipientToken","developer",false,true);

        when(userDao.findUserByUsername("senderTest")).thenReturn(senderUserEntity);
        when(userDao.findUserByUsername("recipientTest")).thenReturn(recipientUserEntity);
        when(userDao.findUserByToken("senderToken")).thenReturn(senderUserEntity);
        when(userDao.findUserByToken("recipientToken")).thenReturn(recipientUserEntity);
        when(messageDao.getMessagesNumberByRecipient(recipientUserEntity)).thenReturn(5); // Assuming there
    }

    @Test
    void testAddMessage_Success() {
        MessageDto messageDto = new MessageDto();
        messageDto.setText("Test message");
        messageDto.setSendDate(LocalDateTime.now());
        messageDto.setSeen(false);
        messageDto.setSenderUsername("sender");
        messageDto.setRecipientUsername("recipient");

        boolean result = messageBean.addMessage(messageDto);

        verify(userDao, times(1)).findUserByUsername("sender");
        verify(userDao, times(1)).findUserByUsername("recipient");

        verify(messageDao, times(1)).persist(any(MessageEntity.class));

        assertTrue(result);
    }

    @Test
    void testGetMessagesSeenFalse_Success() {
        // Mock message entities
        MessageEntity message1 = new MessageEntity("message 1", LocalDateTime.now(), false, userDao.findUserByUsername("senderTest"), userDao.findUserByUsername("recipientTest"));
        MessageEntity message2 = new MessageEntity("message 2", LocalDateTime.now(), false, userDao.findUserByUsername("senderTest"), userDao.findUserByUsername("recipientTest"));

// Mock messageDao to return message entities
        ArrayList<MessageEntity> messageEntities = new ArrayList<>();
        messageEntities.add(message1);
        messageEntities.add(message2);

// Stubbing with matchers for each parameter separately
        when(messageDao.getMessagesSeenFalse(userDao.findUserByUsername("senderTest"), userDao.findUserByUsername("recipientTest")))
                .thenReturn(messageEntities);

        // Call the method under test
        ArrayList<MessageDto> result = messageBean.getMessagesSeenFalse("senderToken", "recipientTest");

        // Verify messageDao.getMessagesSeenFalse was called once with the expected user entities
        verify(messageDao, times(1)).getMessagesSeenFalse(any(UserEntity.class), any(UserEntity.class));

        // Assert that the result is not null and contains the correct number of message DTOs
        assertNotNull(result);
        assertEquals(2, result.size());
        // You may add more assertions to check the content of the message DTOs if needed
    }

    @Test
    void testGetMessagesNumberByToken_Success() {
        // Call the method under test
        int result = messageBean.getMessagesNumberByToken("recipientToken");

        // Verify userDao.findUserByToken was called once with the expected token
        verify(userDao, times(1)).findUserByToken("recipientToken");

        // Verify messageDao.getMessagesNumberByRecipient was called once with the expected recipient
        verify(messageDao, times(1)).getMessagesNumberByRecipient(any(UserEntity.class));

        // Assert that the result is the expected number of messages
        assertEquals(5, result);
    }

    @Test
    void testGetNotifications() {
        // Mock message entities for unseen messages
        MessageEntity unseenMessage1 = new MessageEntity("Unseen message 1", LocalDateTime.now(), false, new UserEntity(), new UserEntity());
        MessageEntity unseenMessage2 = new MessageEntity("Unseen message 2", LocalDateTime.now(), false, new UserEntity(), new UserEntity());
        ArrayList<MessageEntity> unseenMessages = new ArrayList<>();
        unseenMessages.add(unseenMessage1);
        unseenMessages.add(unseenMessage2);

        // Mock message entities for seen messages
        MessageEntity seenMessage1 = new MessageEntity("Seen message 1", LocalDateTime.now(), true, new UserEntity(), new UserEntity());
        MessageEntity seenMessage2 = new MessageEntity("Seen message 2", LocalDateTime.now(), true, new UserEntity(), new UserEntity());
        MessageEntity seenMessage3 = new MessageEntity("Seen message 3", LocalDateTime.now(), true, new UserEntity(), new UserEntity());
        ArrayList<MessageEntity> seenMessages = new ArrayList<>();
        seenMessages.add(seenMessage1);
        seenMessages.add(seenMessage2);
        seenMessages.add(seenMessage3);

        when(messageDao.getMessagesSeenFalseByRecipient(any(UserEntity.class))).thenReturn(unseenMessages);
        when(messageDao.getMessagesSeenTrueByRecipient(any(UserEntity.class))).thenReturn(seenMessages);

        ArrayList<NotificationDto> notifications = messageBean.getNotifications("recipientToken");

        assertNotNull(notifications);
        assertEquals(5, notifications.size()); // Total expected notifications

        for (int i = 0; i < 2; i++) {
            NotificationDto notification = notifications.get(i);
            assertFalse(notification.isSeen()); // Unseen messages should have 'seen' flag set to false
        }

        for (int i = 2; i < 5; i++) {
            NotificationDto notification = notifications.get(i);
            assertTrue(notification.isSeen()); // Seen messages should have 'seen' flag set to true
        }
    }
}