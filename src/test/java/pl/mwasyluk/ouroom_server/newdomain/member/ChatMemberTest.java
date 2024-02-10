package pl.mwasyluk.ouroom_server.newdomain.member;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import pl.mwasyluk.ouroom_server.newdomain.user.User;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatMemberTest {
    private static final User MOCK_USER = User.mockOf(UUID.randomUUID());
    private static final Set<MemberPrivilege> EMPTY_PRIVILEGES = Collections.emptySet();
    private static final Set<MemberPrivilege> NON_EMPTY_PRIVILEGES = Set.of(MemberPrivilege.ADD_MESSAGES);
    private static final Set<MemberPrivilege> MULTI_PRIVILEGES
            = Set.of(MemberPrivilege.ADD_MESSAGES, MemberPrivilege.REMOVE_MESSAGES);

    ChatMember newMemberInstance(User user, Set<MemberPrivilege> privileges) {
        return new ChatMember(user, privileges);
    }

    ChatMember newMemberInstance(Set<MemberPrivilege> privileges) {
        return newMemberInstance(MOCK_USER, privileges);
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTest {
        @Test
        @DisplayName("throws exception when user is null")
        void throwsExceptionWhenUserIsNull() {
            assertThrowsExactly(NullPointerException.class, () -> newMemberInstance(null, NON_EMPTY_PRIVILEGES));
        }

        @Test
        @DisplayName("sets empty privileges when null given")
        void setsEmptyPrivilegesWhenNullGiven() {
            ChatMember o1 = newMemberInstance(null);

            assertAll(() -> {
                assertNotNull(o1.getPrivileges());
                assertTrue(o1.getPrivileges().isEmpty());
            });
        }

        @Test
        @DisplayName("sets empty privileges when empty given")
        void setsEmptyPrivilegesWhenEmptyGiven() {
            ChatMember o1 = newMemberInstance(EMPTY_PRIVILEGES);

            assertAll(() -> {
                assertNotNull(o1.getPrivileges());
                assertTrue(o1.getPrivileges().isEmpty());
            });
        }

        @Test
        @DisplayName("sets privileges when multiple given")
        void setsPrivilegesWhenMultipleGiven() {
            ChatMember o1 = newMemberInstance(MULTI_PRIVILEGES);

            assertAll(() -> {
                assertNotNull(o1.getPrivileges());
                assertEquals(Set.copyOf(MULTI_PRIVILEGES), o1.getPrivileges());
            });
        }
    }

    @Nested
    @DisplayName("setPrivileges method")
    class SetPrivilegesMethodTest {
        @Test
        @DisplayName("returns true and sets empty privileges when null given")
        void returnsTrueAndSetsEmptyPrivilegesWhenNullGiven() {
            ChatMember o1 = newMemberInstance(NON_EMPTY_PRIVILEGES);

            assertAll(() -> {
                assertTrue(o1.setPrivileges(null));
                assertNotNull(o1.getPrivileges());
                assertTrue(o1.getPrivileges().isEmpty());
            });
        }

        @Test
        @DisplayName("returns true and sets empty privileges when empty given")
        void returnsTrueAndSetsEmptyPrivilegesWhenEmptyGiven() {
            ChatMember o1 = newMemberInstance(NON_EMPTY_PRIVILEGES);

            assertAll(() -> {
                assertTrue(o1.setPrivileges(Set.of()));
                assertNotNull(o1.getPrivileges());
                assertTrue(o1.getPrivileges().isEmpty());
            });
        }

        @Test
        @DisplayName("returns true and sets privileges when multiple given")
        void returnsTrueAndSetsPrivilegesWhenMultipleGiven() {
            ChatMember o1 = newMemberInstance(NON_EMPTY_PRIVILEGES);

            assertAll(() -> {
                assertTrue(o1.setPrivileges(MULTI_PRIVILEGES));
                assertNotNull(o1.getPrivileges());
                assertEquals(Set.copyOf(MULTI_PRIVILEGES), o1.getPrivileges());
            });
        }
    }
}
