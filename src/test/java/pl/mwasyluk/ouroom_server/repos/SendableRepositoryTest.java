package pl.mwasyluk.ouroom_server.repos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import pl.mwasyluk.ouroom_server.domain.container.Chat;
import pl.mwasyluk.ouroom_server.domain.sendable.ChatSendable;
import pl.mwasyluk.ouroom_server.domain.user.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class SendableRepositoryTest {
    private final User mockUser = new User("mock", "mock");
    private final User mockUser2 = new User("mock2", "mock");
    private final Chat mockChat = new Chat(mockUser);
    private final Chat mockChat2 = new Chat(mockUser2);

    @Autowired
    @SuppressWarnings("unused")
    private SendableRepository sendableRepository;
    @Autowired
    @SuppressWarnings("unused")
    private ChatRepository chatRepository;
    @Autowired
    @SuppressWarnings("unused")
    private UserRepository userRepository;

    @Nested
    @DisplayName("findAllByContainerId method")
    class FindAllByContainerIdMethodTest {
        @Test
        @DisplayName("returns all sendables with given containerId")
        void returnsAllSendablesWithGivenContainerId() {
            ChatSendable sendable1 = new ChatSendable(mockUser, "text");
            ChatSendable sendable2 = new ChatSendable(mockUser, "text");
            ChatSendable sendable3 = new ChatSendable(mockUser, "text");
            sendable1.setContainer(mockChat);
            sendable2.setContainer(mockChat);
            sendable3.setContainer(mockChat2);
            userRepository.save(mockUser);
            userRepository.save(mockUser2);
            chatRepository.save(mockChat);
            chatRepository.save(mockChat2);
            sendableRepository.save(sendable1);
            sendableRepository.save(sendable2);
            sendableRepository.save(sendable3);

            List<ChatSendable> sendables = sendableRepository.findAllByContainerId(mockChat.getId());

            assertEquals(2, sendables.size());
            assertTrue(sendables.contains(sendable1));
            assertTrue(sendables.contains(sendable2));
        }

        @Test
        @DisplayName("returns empty list when no sendables with given containerId exist")
        void returnsEmptyListWhenNoSendablesWithGivenContainerIdExist() {
            ChatSendable sendable1 = new ChatSendable(mockUser, "text");
            ChatSendable sendable2 = new ChatSendable(mockUser, "text");
            ChatSendable sendable3 = new ChatSendable(mockUser, "text");
            sendable1.setContainer(mockChat);
            sendable2.setContainer(mockChat);
            sendable3.setContainer(mockChat);
            userRepository.save(mockUser);
            userRepository.save(mockUser2);
            chatRepository.save(mockChat);
            chatRepository.save(mockChat2);
            sendableRepository.save(sendable1);
            sendableRepository.save(sendable2);
            sendableRepository.save(sendable3);

            List<ChatSendable> sendables = sendableRepository.findAllByContainerId(mockChat2.getId());

            assertTrue(sendables.isEmpty());
        }
    }
}
