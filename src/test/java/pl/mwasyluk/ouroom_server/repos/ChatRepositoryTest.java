package pl.mwasyluk.ouroom_server.repos;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import pl.mwasyluk.ouroom_server.domain.container.Chat;
import pl.mwasyluk.ouroom_server.domain.member.MemberPrivilege;
import pl.mwasyluk.ouroom_server.domain.sendable.ChatSendable;
import pl.mwasyluk.ouroom_server.domain.user.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ChatRepositoryTest {
    private final User mockUser = new User("mock", "mock");
    private final User mockUser2 = new User("mock2", "mock");
    
    @Autowired
    @SuppressWarnings("unused")
    private UserRepository userRepository;
    @Autowired
    @SuppressWarnings("unused")
    private ChatRepository chatRepository;

    @Nested
    @DisplayName("findAllByUserId method")
    class FindAllByUserIdMethodTest {
        @Test
        @DisplayName("returns all chats where given user is a member")
        void returnsAllChatsWhereGivenUserIsAMember() {
            Chat chat1 = new Chat(mockUser);
            Chat chat2 = new Chat(mockUser);
            Chat chat3 = new Chat(mockUser2);
            chat3.addMember(mockUser, null);

            userRepository.save(mockUser);
            userRepository.save(mockUser2);
            chatRepository.save(chat1);
            chatRepository.save(chat2);
            chatRepository.save(chat3);

            Collection<Chat> chats = chatRepository.findAllByUserId(mockUser.getId());

            assertEquals(3, chats.size());
            assertTrue(chats.contains(chat1));
            assertTrue(chats.contains(chat2));
            assertTrue(chats.contains(chat3));
        }

        @Test
        @DisplayName("returns empty collection when user is not a member of any chat")
        void returnsEmptyCollectionWhenUserIsNotAMemberOfAnyChat() {
            Chat chat1 = new Chat(mockUser2);
            Chat chat2 = new Chat(mockUser2);

            userRepository.save(mockUser);
            userRepository.save(mockUser2);
            chatRepository.save(chat1);
            chatRepository.save(chat2);

            Collection<Chat> chats = chatRepository.findAllByUserId(mockUser.getId());

            assertEquals(0, chats.size());
        }

        @Test
        @DisplayName("returns empty collection when user does not exist")
        void returnsEmptyCollectionWhenUserDoesNotExist() {
            Chat chat1 = new Chat(mockUser);
            Chat chat2 = new Chat(mockUser);

            userRepository.save(mockUser);
            chatRepository.save(chat1);
            chatRepository.save(chat2);

            Collection<Chat> chats = chatRepository.findAllByUserId(mockUser2.getId());

            assertEquals(0, chats.size());
        }
    }

    @Nested
    @DisplayName("findDetailsById method")
    class FindDetailsByIdMethodTest {
        @Test
        @DisplayName("returns chat details by chat ID")
        void returnsChatDetailsByChatId() {
            Chat chat = new Chat(mockUser);
            userRepository.save(mockUser);
            chatRepository.save(chat);

            var chatDetails = chatRepository.findDetailsById(chat.getId());

            assertTrue(chatDetails.isPresent());
            assertEquals(chat.getId(), chatDetails.get().id());
            assertNull(chatDetails.get().name());
            assertNull(chatDetails.get().imageId());
            assertEquals(1, chatDetails.get().membersAmount());
            assertEquals(0, chatDetails.get().sendablesAmount());
        }

        @Test
        @DisplayName("returns empty optional when chat does not exist")
        void returnsEmptyOptionalWhenChatDoesNotExist() {
            var chatDetails = chatRepository.findDetailsById(UUID.randomUUID());

            assertTrue(chatDetails.isEmpty());
        }

        @Test
        @DisplayName("returns correct members amount")
        void returnsCorrectMembersAmount() {
            User mockUser3 = new User("mock3", "mock");
            Chat chat = new Chat(mockUser);
            chat.addMember(mockUser2, null);
            chat.addMember(mockUser3, null);
            userRepository.save(mockUser);
            userRepository.save(mockUser2);
            userRepository.save(mockUser3);
            chatRepository.save(chat);

            var chatDetails = chatRepository.findDetailsById(chat.getId());

            assertTrue(chatDetails.isPresent());
            assertEquals(3, chatDetails.get().membersAmount());
        }

        @Test
        @DisplayName("returns correct sendables amount")
        void returnsCorrectSendablesAmount() {
            Chat chat = new Chat(mockUser);
            chat.addMember(mockUser2, Set.of(MemberPrivilege.ADD_MESSAGES));
            chat.addSendable(new ChatSendable(mockUser, "mock"));
            chat.addSendable(new ChatSendable(mockUser2, "mock2"));
            userRepository.save(mockUser);
            userRepository.save(mockUser2);
            chatRepository.save(chat);

            var chatDetails = chatRepository.findDetailsById(chat.getId());

            assertTrue(chatDetails.isPresent());
            assertEquals(2, chatDetails.get().sendablesAmount());
        }
    }
}
