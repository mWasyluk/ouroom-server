package pl.mwasyluk.ouroom_server.services;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.mwasyluk.ouroom_server.domain.container.Chat;
import pl.mwasyluk.ouroom_server.domain.member.ChatMember;
import pl.mwasyluk.ouroom_server.domain.member.Member;
import pl.mwasyluk.ouroom_server.domain.member.MemberPrivilege;
import pl.mwasyluk.ouroom_server.domain.user.User;
import pl.mwasyluk.ouroom_server.exceptions.ServiceException;
import pl.mwasyluk.ouroom_server.repos.MemberRepository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@ExtendWith(MockitoExtension.class)
class MemberValidatorTest {
    @Mock
    private MemberRepository memberRepo;

    private MemberValidator memberValidator;

    private ChatMember newMockMember(UUID userId, UUID membershipId, Set<MemberPrivilege> privilegeSet) {
        return new ChatMember(User.mockOf(userId), Chat.mockOf(membershipId), privilegeSet);
    }

    @BeforeEach
    void setUp() {
        memberValidator = new MemberValidator(memberRepo);
    }

    @Nested
    @DisplayName("validateAsMember method")
    class ValidateAsMemberMethodTest {
        @Test
        @DisplayName("throws FORBIDDEN when member is not found")
        void throwsForbiddenWhenMemberIsNotFound() {
            UUID userId = UUID.randomUUID();
            UUID chatId = UUID.randomUUID();

            when(memberRepo.findById(any())).thenReturn(Optional.empty());

            ServiceException serviceException = assertThrows(
                    ServiceException.class, () -> memberValidator.validateAsMember(userId, chatId));
            assertEquals(FORBIDDEN, serviceException.getStatusCode());
        }

        @Test
        @DisplayName("returns member when member is found")
        void returnsMemberWhenMemberIsFound() {
            UUID userId = UUID.randomUUID();
            UUID chatId = UUID.randomUUID();

            when(memberRepo.findById(any())).thenReturn(
                    Optional.of(newMockMember(userId, chatId, Set.of())));

            assertDoesNotThrow(() -> memberValidator.validateAsMember(userId, chatId));
        }
    }

    @Nested
    @DisplayName("validatePrivilegesAsMember method")
    class ValidatePrivilegesAsMemberMethodTest {
        @Test
        @DisplayName("throws FORBIDDEN when member does not have any privileges")
        void throwsForbiddenWhenMemberDoesNotHaveAnyPrivileges() {
            UUID userId = UUID.randomUUID();
            UUID chatId = UUID.randomUUID();

            when(memberRepo.findById(any())).thenReturn(
                    Optional.of(newMockMember(userId, chatId, Set.of())));

            ServiceException serviceException = assertThrows(
                    ServiceException.class, () -> memberValidator.validatePrivilegesAsMember(
                            userId, chatId, EnumSet.of(MemberPrivilege.ADD_MESSAGES)));
            assertEquals(FORBIDDEN, serviceException.getStatusCode());
        }

        @Test
        @DisplayName("returns member when member does not have any privileges and non required")
        void returnsMemberWhenMemberDoesNotHaveAnyPrivilegesAndNonRequired() {
            UUID userId = UUID.randomUUID();
            UUID chatId = UUID.randomUUID();

            when(memberRepo.findById(any())).thenReturn(
                    Optional.of(newMockMember(userId, chatId, Set.of())));

            Member member = assertDoesNotThrow(() -> memberValidator.validatePrivilegesAsMember(
                    userId, chatId, EnumSet.noneOf(MemberPrivilege.class)));
            assertNotNull(member);
        }

        @Test
        @DisplayName("throws FORBIDDEN when member does not have required privileges")
        void throwsForbiddenWhenMemberDoesNotHaveRequiredPrivileges() {
            UUID userId = UUID.randomUUID();
            UUID chatId = UUID.randomUUID();

            when(memberRepo.findById(any())).thenReturn(
                    Optional.of(newMockMember(userId, chatId,
                            Set.of(MemberPrivilege.DELETE_MESSAGES, MemberPrivilege.MANAGE_MEMBERS))));

            ServiceException serviceException = assertThrows(
                    ServiceException.class, () -> memberValidator.validatePrivilegesAsMember(
                            userId, chatId, MemberPrivilege.ADD_MESSAGES));
            assertEquals(FORBIDDEN, serviceException.getStatusCode());
        }

        @Test
        @DisplayName("throws FORBIDDEN when member does not have all required privileges")
        void throwsForbiddenWhenMemberDoesNotHaveAllRequiredPrivileges() {
            UUID userId = UUID.randomUUID();
            UUID chatId = UUID.randomUUID();

            when(memberRepo.findById(any())).thenReturn(
                    Optional.of(newMockMember(userId, chatId,
                            Set.of(MemberPrivilege.DELETE_MESSAGES, MemberPrivilege.MANAGE_MEMBERS))));

            ServiceException serviceException = assertThrows(
                    ServiceException.class, () -> memberValidator.validatePrivilegesAsMember(
                            userId, chatId, EnumSet.of(MemberPrivilege.DELETE_MESSAGES, MemberPrivilege.ADD_MESSAGES)));
            assertEquals(FORBIDDEN, serviceException.getStatusCode());
        }

        @Test
        @DisplayName("returns member when member has exactly required privileges")
        void returnsMemberWhenMemberHasExactlyRequiredPrivileges() {
            UUID userId = UUID.randomUUID();
            UUID chatId = UUID.randomUUID();

            when(memberRepo.findById(any())).thenReturn(
                    Optional.of(newMockMember(userId, chatId,
                            Set.of(MemberPrivilege.DELETE_MESSAGES, MemberPrivilege.MANAGE_MEMBERS))));

            Member member = assertDoesNotThrow(() -> memberValidator.validatePrivilegesAsMember(
                    userId, chatId, EnumSet.of(MemberPrivilege.DELETE_MESSAGES, MemberPrivilege.MANAGE_MEMBERS)));
            assertNotNull(member);
        }

        @Test
        @DisplayName("returns member when member has more than required privileges")
        void returnsMemberWhenMemberHasMoreThanRequiredPrivileges() {
            UUID userId = UUID.randomUUID();
            UUID chatId = UUID.randomUUID();

            when(memberRepo.findById(any())).thenReturn(
                    Optional.of(newMockMember(userId, chatId,
                            Set.of(MemberPrivilege.DELETE_MESSAGES, MemberPrivilege.MANAGE_MEMBERS,
                                    MemberPrivilege.ADD_MESSAGES))));

            Member member = assertDoesNotThrow(() -> memberValidator.validatePrivilegesAsMember(
                    userId, chatId, EnumSet.of(MemberPrivilege.DELETE_MESSAGES, MemberPrivilege.MANAGE_MEMBERS)));
            assertNotNull(member);
        }

        @Test
        @DisplayName("returns member when member has exactly one required privilege")
        void returnsMemberWhenMemberHasExactlyOneRequiredPrivilege() {
            UUID userId = UUID.randomUUID();
            UUID chatId = UUID.randomUUID();

            when(memberRepo.findById(any())).thenReturn(
                    Optional.of(newMockMember(userId, chatId,
                            Set.of(MemberPrivilege.DELETE_MESSAGES, MemberPrivilege.MANAGE_MEMBERS))));

            Member member = assertDoesNotThrow(() -> memberValidator.validatePrivilegesAsMember(
                    userId, chatId, MemberPrivilege.DELETE_MESSAGES));
            assertNotNull(member);
        }
    }
}
