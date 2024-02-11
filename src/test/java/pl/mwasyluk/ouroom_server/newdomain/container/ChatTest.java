package pl.mwasyluk.ouroom_server.newdomain.container;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import pl.mwasyluk.ouroom_server.newdomain.member.Member;
import pl.mwasyluk.ouroom_server.newdomain.member.MemberPrivilege;
import pl.mwasyluk.ouroom_server.newdomain.user.User;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatTest {
    private static final User MOCK_USER = User.mockOf(UUID.randomUUID());
    private static final Set<MemberPrivilege> EMPTY_PRIVILEGES = Collections.emptySet();
    private static final Set<MemberPrivilege> SINGLE_PRIVILEGES = Set.of(MemberPrivilege.ADD_MESSAGES);
    private static final Set<MemberPrivilege> MULTI_PRIVILEGES
            = Set.of(MemberPrivilege.ADD_MESSAGES, MemberPrivilege.REMOVE_MESSAGES);
    private static final Set<MemberPrivilege> ALL_PRIVILEGES = Set.of(MemberPrivilege.values());
    private static final Set<MemberPrivilege> INCOMPLETE_PRIVILEGES
            = ALL_PRIVILEGES.stream().skip(1).collect(Collectors.toSet());

    Chat newOf(User user, Map<User, Set<MemberPrivilege>> memberUserPrivilegesMap) {
        return new Chat(user, memberUserPrivilegesMap);
    }

    Chat newOf(Map<User, Set<MemberPrivilege>> memberUserPrivilegesMap) {
        return newOf(MOCK_USER, memberUserPrivilegesMap);
    }

    User newMockUser() {
        return User.mockOf(UUID.randomUUID());
    }

    void assertMembersContains(Chat c, UUID expectedUserId) {
        Optional<Member> optionalMemberByUserId = c.getMemberByUserId(expectedUserId);

        assertTrue(optionalMemberByUserId.isPresent());
    }

    void assertMembersContains(Chat c, UUID expectedUserId, Set<MemberPrivilege> expectedPrivileges) {
        Optional<Member> optionalMemberByUserId = c.getMemberByUserId(expectedUserId);

        assertAll(() -> {
            assertTrue(optionalMemberByUserId.isPresent());
            assertEquals(expectedPrivileges, optionalMemberByUserId.get().getPrivileges());
        });
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTest {
        @Test
        @DisplayName("throws exception when null user given")
        void throwsExceptionWhenNullUserGiven() {
            assertThrowsExactly(NullPointerException.class, () -> newOf(null, null));
        }

        @Test
        @DisplayName("adds admin into members array with appropriate privileges when map null empty or not empty")
        void addsAdminIntoMembersArrayWithAppropriatePrivilegesWhenMapNullEmptyOrNotEmpty() {
            Chat o1 = newOf(null);
            Chat o2 = newOf(Map.of());
            Chat o3 = newOf(Map.of(newMockUser(), Set.of()));

            assertAll(() -> {
                assertMembersContains(o1, MOCK_USER.getId(), Chat.ADMIN_PRIVILEGES);
                assertMembersContains(o2, MOCK_USER.getId(), Chat.ADMIN_PRIVILEGES);
                assertMembersContains(o3, MOCK_USER.getId(), Chat.ADMIN_PRIVILEGES);
            });
        }

        @Test
        @DisplayName("adds all with appropriate privileges when map not empty")
        void addsAllWithAppropriatePrivilegesWhenMapNotEmpty() {
            var u1 = newMockUser();
            var u2 = newMockUser();
            var u3 = newMockUser();
            var u4 = newMockUser();
            var u5 = newMockUser();
            var u6 = newMockUser();
            Set<MemberPrivilege> p1 = null;
            Set<MemberPrivilege> p2 = Set.of();
            Set<MemberPrivilege> p3 = Set.of(MemberPrivilege.MANAGE_MEMBERS);
            Set<MemberPrivilege> p4 = Set.of(MemberPrivilege.ADD_MESSAGES, MemberPrivilege.PIN_MESSAGES,
                    MemberPrivilege.REMOVE_MESSAGES);
            Set<MemberPrivilege> p5 = Chat.ADMIN_PRIVILEGES;
            Set<MemberPrivilege> p6 = Chat.DEFAULT_PRIVILEGES;

            HashMap<User, Set<MemberPrivilege>> members = new HashMap<>() {{
                put(u1, p1);
                put(u2, p2);
                put(u3, p3);
                put(u4, p4);
                put(u5, p5);
                put(u6, p6);
            }};
            Chat o1 = newOf(members);

            assertAll(() -> {
                        assertMembersContains(o1, u1.getId(), Set.of());
                        assertMembersContains(o1, u2.getId(), p2);
                        assertMembersContains(o1, u3.getId(), p3);
                        assertMembersContains(o1, u4.getId(), p4);
                        assertMembersContains(o1, u5.getId(), p5);
                        assertMembersContains(o1, u6.getId(), p6);
                    }
            );
        }

        @Test
        @DisplayName("does not add admin twice when appears in map")
        void doesNotAddAdminTwiceWhenAppearsInMap() {
            Chat o1 = newOf(Map.of(MOCK_USER, Set.of(MemberPrivilege.ADD_MESSAGES)));

            assertAll(() -> {
                assertEquals(1, o1.getAllMembers().size());
                assertMembersContains(o1, MOCK_USER.getId(), Chat.ADMIN_PRIVILEGES);
            });
        }

        @Test
        @DisplayName("adds additional admins when they appear in map")
        void addsAdditionalAdminsWhenTheyAppearInMap() {
            var u1 = newMockUser();
            var u2 = newMockUser();

            HashMap<User, Set<MemberPrivilege>> members = new HashMap<>() {{
                put(u1, Chat.ADMIN_PRIVILEGES);
                put(u2, Chat.ADMIN_PRIVILEGES);
            }};
            Chat o1 = newOf(members);

            assertAll(() -> {
                        assertMembersContains(o1, u1.getId(), Chat.ADMIN_PRIVILEGES);
                        assertMembersContains(o1, u2.getId(), Chat.ADMIN_PRIVILEGES);
                    }
            );
        }
    }

    @Nested
    @DisplayName("isAdminByUserId method")
    class IsAdminByUserIdMethodTest {
        @Test
        @DisplayName("returns true when user has all privileges")
        void returnsTrueWhenUserHasAllPrivileges() {
            var u1 = newMockUser();
            var u2 = newMockUser();

            HashMap<User, Set<MemberPrivilege>> members = new HashMap<>() {{
                put(u1, Set.of(MemberPrivilege.values()));
            }};
            Chat o1 = newOf(members);
            o1.addMember(u2, Set.of(MemberPrivilege.values()));

            assertAll(() -> {
                assertTrue(o1.isAdminByUserId(MOCK_USER.getId()));
                assertTrue(o1.isAdminByUserId(u1.getId()));
                assertTrue(o1.isAdminByUserId(u2.getId()));
            });
        }

        @Test
        @DisplayName("returns false when user lacks of single privilege")
        void returnsFalseWhenUserLacksOfSinglePrivilege() {
            var u1 = newMockUser();
            var u2 = newMockUser();

            HashMap<User, Set<MemberPrivilege>> members = new HashMap<>() {{
                put(u1, INCOMPLETE_PRIVILEGES);
            }};
            Chat o1 = newOf(members);
            o1.addMember(u2, INCOMPLETE_PRIVILEGES);

            assertAll(() -> {
                assertFalse(o1.isAdminByUserId(u1.getId()));
                assertFalse(o1.isAdminByUserId(u2.getId()));
            });
        }

        @Test
        @DisplayName("returns true when user has been updated to all privileges")
        void returnsTrueWhenUserHasBeenUpdatedToAllPrivileges() {
            var u1 = newMockUser();
            var u2 = newMockUser();

            HashMap<User, Set<MemberPrivilege>> members = new HashMap<>() {{
                put(u1, Set.of(MemberPrivilege.ADD_MESSAGES));
                put(u2, Set.of(MemberPrivilege.ADD_MESSAGES));
            }};
            Chat o1 = newOf(members);

            o1.getMemberByUserId(u1.getId()).get()
                    .setPrivileges(Set.of(MemberPrivilege.values()));
            o1.getAllMembers().stream().filter(m -> m.getUser().getId().equals(u2.getId())).findAny().get()
                    .setPrivileges(Set.of(MemberPrivilege.values()));

            assertAll(() -> {
                assertTrue(o1.isAdminByUserId(u1.getId()));
                assertTrue(o1.isAdminByUserId(u2.getId()));
            });
        }

        @Test
        @DisplayName("returns false when admin user has been updated to incomplete privileges")
        void returnsFalseWhenAdminUserHasBeenUpdatedToIncompletePrivileges() {
            var u1 = newMockUser();
            var u2 = newMockUser();

            HashMap<User, Set<MemberPrivilege>> members = new HashMap<>() {{
                put(u1, Set.of(MemberPrivilege.values()));
                put(u2, Set.of(MemberPrivilege.values()));
            }};
            Chat o1 = newOf(members);

            o1.getMemberByUserId(u1.getId()).get()
                    .setPrivileges(INCOMPLETE_PRIVILEGES);
            o1.getAllMembers().stream().filter(m -> m.getUser().getId().equals(u2.getId())).findAny().get()
                    .setPrivileges(INCOMPLETE_PRIVILEGES);

            assertAll(() -> {
                assertFalse(o1.isAdminByUserId(u1.getId()));
                assertFalse(o1.isAdminByUserId(u2.getId()));
            });
        }

        @Test
        @DisplayName("returns false when user has no privileges")
        void returnsFalseWhenUserHasNoPrivileges() {
            var u1 = newMockUser();
            var u2 = newMockUser();

            HashMap<User, Set<MemberPrivilege>> members = new HashMap<>() {{
                put(u1, Set.of());
                put(u2, Set.of(MemberPrivilege.values()));
            }};
            Chat o1 = newOf(members);

            o1.getMemberByUserId(u2.getId()).get()
                    .setPrivileges(null);

            assertAll(() -> {
                assertFalse(o1.isAdminByUserId(u1.getId()));
                assertFalse(o1.isAdminByUserId(u2.getId()));
            });
        }
    }

    @Nested
    @DisplayName("hasAnyAdmin method")
    class HasAnyAdminMethodTest {
        @Test
        @DisplayName("returns true when just initialized with null map")
        void returnsTrueWhenJustInitializedWithNullMap() {
            Chat o1 = newOf(null);

            assertTrue(o1.hasAnyAdmin());
        }

        @Test
        @DisplayName("returns false when main admin has been updated with incomplete privileges")
        void returnsFalseWhenMainAdminHasBeenUpdatedWithIncompletePrivileges() {
            Chat o1 = newOf(null);
            o1.getMemberByUserId(MOCK_USER.getId()).get().setPrivileges(INCOMPLETE_PRIVILEGES);

            assertFalse(o1.hasAnyAdmin());
        }

        @Test
        @DisplayName("returns false when the only admin has been removed")
        void returnsFalseWhenTheOnlyAdminHasBeenRemoved() {
            Chat o1 = newOf(null);
            o1.removeMemberByUserId(MOCK_USER.getId());

            assertFalse(o1.hasAnyAdmin());
        }

        @Test
        @DisplayName("returns true when main admin has been removed but other exists")
        void returnsTrueWhenMainAdminHasBeenRemovedButOtherExists() {
            Chat o1 = newOf(Map.of(newMockUser(), Chat.ADMIN_PRIVILEGES));
            o1.removeMemberByUserId(MOCK_USER.getId());

            assertTrue(o1.hasAnyAdmin());
        }

        @Test
        @DisplayName("returns true when main admin has been removed but other added")
        void returnsTrueWhenMainAdminHasBeenRemovedButOtherAdded() {
            Chat o1 = newOf(null);
            o1.removeMemberByUserId(MOCK_USER.getId());
            o1.addMember(newMockUser(), Chat.ADMIN_PRIVILEGES);

            assertTrue(o1.hasAnyAdmin());
        }
    }

    @Nested
    @DisplayName("addMember method")
    class AddMemberMethodTest {
        @Test
        @DisplayName("returns false when user is member ignoring privileges")
        void returnsFalseWhenUserIsMemberIgnoringPrivileges() {
            var u1 = newMockUser();
            Chat o1 = newOf(Map.of(u1, Set.of(MemberPrivilege.ADD_MESSAGES)));

            assertAll(() -> {
                assertFalse(o1.addMember(MOCK_USER, Set.of()));
                assertFalse(o1.addMember(MOCK_USER, Chat.ADMIN_PRIVILEGES));
                assertFalse(o1.addMember(u1, Chat.ADMIN_PRIVILEGES));
                assertFalse(o1.addMember(u1, Set.of(MemberPrivilege.ADD_MESSAGES)));
            });
        }

        @Test
        @DisplayName("sets empty privileges when null given")
        void setsEmptyPrivilegesWhenNullGiven() {
            var u1 = newMockUser();
            Chat o1 = newOf(null);

            o1.addMember(u1, null);

            Set<MemberPrivilege> privileges = o1.getMemberByUserId(u1.getId()).get().getPrivileges();

            assertAll(() -> {
                assertNotNull(privileges);
                assertEquals(0, privileges.size());
            });
        }

        @Test
        @DisplayName("returns true when user is not member")
        void returnsTrueWhenUserIsNotMember() {
            var u1 = newMockUser();
            Chat o1 = newOf(null);

            assertTrue(o1.addMember(u1, Set.of()));
        }

        @Test
        @DisplayName("adds to members and sets membership")
        void addsToMembersAndSetsMembership() {
            var u1 = newMockUser();
            Chat o1 = newOf(null);

            o1.addMember(u1, MULTI_PRIVILEGES);

            assertAll(() -> {
                assertMembersContains(o1, u1.getId());
                assertEquals(o1, o1.getMemberByUserId(u1.getId()).get().getMembership());
            });
        }

        @Test
        @DisplayName("adds admin member")
        void addsAdminMember() {
            var u1 = newMockUser();
            Chat o1 = newOf(null);

            o1.addMember(u1, Chat.ADMIN_PRIVILEGES);

            assertTrue(o1.isAdminByUserId(u1.getId()));
        }
    }
}
