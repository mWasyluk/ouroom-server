package pl.mwasyluk.ouroom_server.repos;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import pl.mwasyluk.ouroom_server.domain.container.Chat;
import pl.mwasyluk.ouroom_server.domain.member.ChatMember;
import pl.mwasyluk.ouroom_server.domain.user.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class MemberRepositoryTest {
    private final User mockUser = new User("mock", "mock");
    private final User mockUser2 = new User("mock2", "mock");
    private final User mockUser3 = new User("mock3", "mock");
    private final Chat mockChat = new Chat(mockUser);

    @Autowired
    @SuppressWarnings("unused")
    private MemberRepository memberRepository;
    @Autowired
    @SuppressWarnings("unused")
    private UserRepository userRepository;
    @Autowired
    @SuppressWarnings("unused")
    private ChatRepository chatRepository;

    @BeforeEach
    void setUp() {
        userRepository.save(mockUser);
        userRepository.save(mockUser2);
        userRepository.save(mockUser3);
    }

    @Nested
    @DisplayName("anyExists method")
    class AnyExistsMethodTest {
        @Test
        @DisplayName("returns false when user is a member but of another chat")
        void returnsFalseWhenUserIsAMemberButOfAnotherChat() {
            Chat mockChat2 = new Chat(mockUser2);
            chatRepository.save(mockChat);
            chatRepository.save(mockChat2);

            assertFalse(memberRepository.anyExists(Set.of(mockUser.getId()), mockChat2.getId()));
        }

        @Test
        @DisplayName("returns true when any member exists")
        void returnsTrueWhenAnyMemberExists() {
            mockChat.addMember(mockUser2, null);
            mockChat.addMember(mockUser3, null);
            chatRepository.save(mockChat);

            assertTrue(memberRepository
                    .anyExists(Set.of(UUID.randomUUID(), mockUser2.getId()), mockChat.getId()));
        }

        @Test
        @DisplayName("returns false when no members exist")
        void returnsFalseWhenNoMembersExist() {
            mockChat.addMember(mockUser2, null);
            chatRepository.save(mockChat);

            assertFalse(memberRepository.anyExists(Set.of(mockUser3.getId()), mockChat.getId()));
        }
    }

    @Nested
    @DisplayName("findAllByUserIdIn method")
    class FindAllByUserIdInMethodTest {
        @Test
        @DisplayName("returns all members with given userIds when exist")
        void returnsAllMembersWithGivenUserIdsWhenExist() {
            User mockUser4 = new User("mock4", "mock");
            Chat mockChat2 = new Chat(mockUser4);
            mockChat.addMember(mockUser2, null);
            mockChat.addMember(mockUser3, null);
            userRepository.save(mockUser4);
            chatRepository.save(mockChat);
            chatRepository.save(mockChat2);

            List<ChatMember> members = memberRepository
                    .findAllByUserIdIn(Set.of(mockUser2.getId(), mockUser3.getId()), mockChat.getId());

            assertEquals(2, members.size());
            assertTrue(members.stream().anyMatch(m -> m.getUser().equals(mockUser2)));
            assertTrue(members.stream().anyMatch(m -> m.getUser().equals(mockUser3)));
        }

        @Test
        @DisplayName("returns empty set when no members with given userIds exist")
        void returnsEmptySetWhenNoMembersWithGivenUserIdsExist() {
            User mockUser4 = new User("mock4", "mock");
            Chat mockChat2 = new Chat(mockUser4);
            mockChat.addMember(mockUser2, null);
            userRepository.save(mockUser4);
            chatRepository.save(mockChat);
            chatRepository.save(mockChat2);

            List<ChatMember> members = memberRepository
                    .findAllByUserIdIn(Set.of(mockUser3.getId(), mockUser4.getId()), mockChat.getId());

            assertEquals(0, members.size());
        }

        @Test
        @DisplayName("returns empty set when chat does not exist")
        void returnsEmptySetWhenChatDoesNotExist() {
            mockChat.addMember(mockUser2, null);
            chatRepository.save(mockChat);

            List<ChatMember> members = memberRepository
                    .findAllByUserIdIn(Set.of(mockUser2.getId(), mockUser3.getId()), UUID.randomUUID());

            assertEquals(0, members.size());
        }

        @Test
        @DisplayName("returns empty set when empty userIds set provided")
        void returnsEmptySetWhenEmptyUserIdsSetProvided() {
            mockChat.addMember(mockUser2, null);
            chatRepository.save(mockChat);

            List<ChatMember> members = memberRepository
                    .findAllByUserIdIn(Set.of(), mockChat.getId());

            assertEquals(0, members.size());
        }

        @Test
        @DisplayName("returns empty set when null userIds set provided")
        void returnsEmptySetWhenNullUserIdsSetProvided() {
            mockChat.addMember(mockUser2, null);
            chatRepository.save(mockChat);

            List<ChatMember> members = memberRepository
                    .findAllByUserIdIn(null, mockChat.getId());

            assertEquals(0, members.size());
        }
    }

    @Nested
    @DisplayName("findAllByMembershipId method")
    class FindAllByMembershipIdMethodTest {
        @Test
        @DisplayName("returns all members with given membershipId when exist")
        void returnsAllMembersWithGivenMembershipIdWhenExist() {
            User mockUser4 = new User("mock4", "mock");
            Chat mockChat2 = new Chat(mockUser2);
            Chat mockChat3 = new Chat(mockUser4);
            mockChat.addMember(mockUser2, null);
            mockChat.addMember(mockUser3, null);
            userRepository.save(mockUser4);
            chatRepository.save(mockChat);
            chatRepository.save(mockChat2);
            chatRepository.save(mockChat3);

            List<ChatMember> members = memberRepository
                    .findAllByMembershipId(mockChat.getId());

            assertEquals(3, members.size());
            assertTrue(members.stream().anyMatch(m -> m.getUser().equals(mockUser)));
            assertTrue(members.stream().anyMatch(m -> m.getUser().equals(mockUser2)));
            assertTrue(members.stream().anyMatch(m -> m.getUser().equals(mockUser3)));
        }

        @Test
        @DisplayName("returns empty set when no members with given membershipId exist")
        void returnsEmptySetWhenNoMembersWithGivenMembershipIdExist() {
            Chat mockChat2 = new Chat(mockUser2);
            mockChat.addMember(mockUser2, null);
            chatRepository.save(mockChat);

            List<ChatMember> members = memberRepository
                    .findAllByMembershipId(mockChat2.getId());

            assertEquals(0, members.size());
        }

        @Test
        @DisplayName("returns empty set when chat does not exist")
        void returnsEmptySetWhenChatDoesNotExist() {
            mockChat.addMember(mockUser2, null);
            chatRepository.save(mockChat);

            List<ChatMember> members = memberRepository
                    .findAllByMembershipId(UUID.randomUUID());

            assertEquals(0, members.size());
        }
    }
}

















