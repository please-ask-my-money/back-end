package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.domain.*;
import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.domain.user.repository.UserProfileRepository;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableJpaAuditing
class MessageRepositoryTest {

    @Autowired
    MessageRepository messageRepository;
    @Autowired
    ChatRepository chatRepository;
    @Autowired
    ChatMessageRepository chatMessageRepository;
    @Autowired
    ChannelRepository channelRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserProfileRepository userProfileRepository;
    @PersistenceContext
    EntityManager em;
    @BeforeEach
    void setup() {
        // 테스트 유저 4명 등록
        User testUser1 = User.builder().role(Role.USER).userKey("test_1").password("").build();
        User testUser2 = User.builder().role(Role.USER).userKey("test_2").password("").build();
        User testUser3 = User.builder().role(Role.USER).userKey("test_3").password("").build();
        User testUser4 = User.builder().role(Role.USER).userKey("test_4").password("").build();

        userRepository.save(testUser1);
        userRepository.save(testUser2);
        userRepository.save(testUser3);
        userRepository.save(testUser4);

        UserProfile userProfile1 = UserProfile.builder().nickname("김씨").imgUrl("imgUrl").intro("테스트 유저 1 입니다.").birth("19900000").gender(Gender.MALE).phoneNumber("01012345678").build();
        UserProfile userProfile2 = UserProfile.builder().nickname("안씨").imgUrl("imgUrl").intro("테스트 유저 2 입니다.").birth("19900000").gender(Gender.MALE).phoneNumber("01012345678").build();
        UserProfile userProfile3 = UserProfile.builder().nickname("이씨").imgUrl("imgUrl").intro("테스트 유저 3 입니다.").birth("19900000").gender(Gender.MALE).phoneNumber("01012345678").build();
        UserProfile userProfile4 = UserProfile.builder().nickname("박씨").imgUrl("imgUrl").intro("테스트 유저 4 입니다.").birth("19900000").gender(Gender.MALE).phoneNumber("01012345678").build();

        userProfileRepository.save(userProfile1);
        userProfileRepository.save(userProfile2);
        userProfileRepository.save(userProfile3);
        userProfileRepository.save(userProfile4);

        testUser1.setUserProfile(userProfile1);
        testUser2.setUserProfile(userProfile2);
        testUser3.setUserProfile(userProfile3);
        testUser3.setUserProfile(userProfile4);

        // 채팅 채널 생성
        PrivateChannel channel1 = PrivateChannel.builder().build();
        GroupChannel channel2 = GroupChannel.builder()
                .channelType(ChannelType.GROUP)
                .channelLimit(5)
                .defaultTitle("기본 설정 방제")
                .owner(testUser2)
                .build();

        channelRepository.save(channel1);
        channelRepository.save(channel2);

        // 채팅 생성
        // 채팅 채널 1 = chat1, chat2
        // 채팅 채널 2 = chat3, chat4, chat5
        Chat chat1 = Chat.builder().owner(testUser1)
                .title(testUser2.getUserProfile().getNickname())
                .channel(channel1).build();

        Chat chat2 = Chat.builder().owner(testUser2)
                .title(testUser1.getUserProfile().getNickname())
                .channel(channel1).build();

        Chat chat3 = Chat.builder().owner(testUser2)
                .title(channel2.getDefaultTitle())
                .channel(channel2).build();

        Chat chat4 = Chat.builder().owner(testUser3)
                .title(channel2.getDefaultTitle())
                .channel(channel2).build();

        Chat chat5 = Chat.builder().owner(testUser4)
                .title(channel2.getDefaultTitle())
                .channel(channel2).build();

        chatRepository.save(chat1);
        chatRepository.save(chat2);
        chatRepository.save(chat3);
        chatRepository.save(chat4);
        chatRepository.save(chat5);

        // Message 생성
        Message msg1 = Message.builder().content("처음 뵙겠습니다.").senderId(testUser2.getId()).channel(channel2).build();
        Message msg2 = Message.builder().content("두번째 인데요.").senderId(testUser3.getId()).channel(channel2).build();
        messageRepository.save(msg1);
        messageRepository.save(msg2);

        // Chat - Message 연결
        ChatMessage chatMessage1 = ChatMessage.builder().message(msg1).isRead(false).chat(chat3).build();
        ChatMessage chatMessage2 = ChatMessage.builder().message(msg1).isRead(true).chat(chat4).build();
        ChatMessage chatMessage3 = ChatMessage.builder().message(msg2).isRead(true).chat(chat5).build();
        ChatMessage chatMessage4 = ChatMessage.builder().message(msg2).isRead(true).chat(chat3).build();
        ChatMessage chatMessage5 = ChatMessage.builder().message(msg2).isRead(true).chat(chat4).build();
        ChatMessage chatMessage6 = ChatMessage.builder().message(msg2).isRead(true).chat(chat5).build();

        chatMessageRepository.save(chatMessage1);
        chatMessageRepository.save(chatMessage2);
        chatMessageRepository.save(chatMessage3);
        chatMessageRepository.save(chatMessage4);
        chatMessageRepository.save(chatMessage5);
        chatMessageRepository.save(chatMessage6);

        channel1.updateLastMessage(msg1.getContent());
        channel2.updateLastMessage(msg2.getContent());

        em.flush();
    }

    @Nested
    class findRecentMessages {
        @Test
        @DisplayName("조회 성공")
        void test1() {
            // given
            User testUser2 = userRepository.findByUserKey("test_2").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            Channel groupChannel = channelRepository.findGroupChannelByOwnerId(testUser2.getId()).orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));
            Chat chat = chatRepository.findChatByChannelIdAndOwnerId(groupChannel.getId(), testUser2.getId()).orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));

            // when
            Page<Message> result = messageRepository.findRecentMessages(chat.getId(), Pageable.ofSize(5));

            // then
            assertEquals(2, result.getContent().size());
            assertEquals("두번째 인데요.", result.getContent().get(0).getContent());
        }
    }

    @Nested
    class deleteMessagesByChannelId {
        @Test
        @Transactional
        @DisplayName("삭제 성공")
        void test1() {
            // given
            User testUser2 = userRepository.findByUserKey("test_2").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            Channel groupChannel = channelRepository.findGroupChannelByOwnerId(testUser2.getId()).orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));
            Chat chat = chatRepository.findChatByChannelIdAndOwnerId(groupChannel.getId(), testUser2.getId()).orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));

            // when
            List<Chat> chatList = chatRepository.findByChannelId(groupChannel.getId());
            chatList.forEach(c -> chatMessageRepository.deleteChatMessageByChatId(c.getId()));
            em.flush(); em.clear();
            messageRepository.deleteMessagesByChannelId(groupChannel.getId());
            em.flush(); em.clear();
            Page<Message> result = messageRepository.findRecentMessages(chat.getId(), Pageable.ofSize(5));

            // then
            assertEquals(0, result.getContent().size());
        }
    }


}