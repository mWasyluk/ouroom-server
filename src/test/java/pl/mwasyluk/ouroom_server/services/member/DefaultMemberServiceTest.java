package pl.mwasyluk.ouroom_server.services.member;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.mwasyluk.ouroom_server.domain.container.Chat;
import pl.mwasyluk.ouroom_server.domain.member.ChatMember;
import pl.mwasyluk.ouroom_server.domain.member.MemberPrivilege;
import pl.mwasyluk.ouroom_server.domain.user.User;
import pl.mwasyluk.ouroom_server.domain.user.UserAuthority;
import pl.mwasyluk.ouroom_server.dto.member.MemberPresentableView;
import pl.mwasyluk.ouroom_server.dto.member.MembersForm;
import pl.mwasyluk.ouroom_server.dto.notification.NotificationView;
import pl.mwasyluk.ouroom_server.exceptions.ServiceException;
import pl.mwasyluk.ouroom_server.exceptions.UnexpectedStateException;
import pl.mwasyluk.ouroom_server.mocks.WithMockCustomUser;
import pl.mwasyluk.ouroom_server.repos.ChatRepository;
import pl.mwasyluk.ouroom_server.repos.MemberRepository;
import pl.mwasyluk.ouroom_server.repos.UserRepository;
import pl.mwasyluk.ouroom_server.services.MemberValidator;
import pl.mwasyluk.ouroom_server.websocket.NotificationTemplate;
import pl.mwasyluk.ouroom_server.websocket.Topic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static pl.mwasyluk.ouroom_server.mocks.WithUserDetailsSecurityContextFactory.pullPrincipalUser;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig
class DefaultMemberServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ChatRepository chatRepository;
    @Mock
    private MemberValidator memberValidator;
    @Mock
    private NotificationTemplate notificationTemplate;

    private DefaultMemberService memberService;
    private User mockUser1;
    private User mockUser2;
    private User mockUser3;
    private Set<MemberPrivilege> privileges1;
    private Set<MemberPrivilege> privileges2;
    private Set<MemberPrivilege> privileges3;

    @BeforeEach
    void setUp() {
        memberService = new DefaultMemberService(userRepository, memberRepository, chatRepository, memberValidator,
                notificationTemplate);
        mockUser1 = new User("test", "pass", Set.of(UserAuthority.USER));
        mockUser2 = new User("test", "pass", Set.of(UserAuthority.USER));
        mockUser3 = new User("test", "pass", Set.of(UserAuthority.USER));

        privileges1 = Set.of(MemberPrivilege.ADD_MESSAGES);
        privileges2 = Set.of(MemberPrivilege.ADD_MESSAGES, MemberPrivilege.MANAGE_MEMBERS);
        privileges3 =
                Set.of(MemberPrivilege.ADD_MESSAGES, MemberPrivilege.DELETE_MESSAGES, MemberPrivilege.PIN_MESSAGES);
    }

    private void whenNotValidAsMember(User user) {
        when(memberValidator.validateAsMember(
                eq(user.getId()), any(UUID.class)))
                .thenThrow(new ServiceException(FORBIDDEN, ""));
    }

    private void whenValidAsMember(User user, Chat chat) {
        when(memberValidator.validateAsMember(
                eq(user.getId()), any(UUID.class)))
                .thenReturn(new ChatMember(user, chat, Set.of()));
    }

    private void whenNotValidPrivilegeAsMember(User user) {
        when(memberValidator.validatePrivilegesAsMember(
                eq(user.getId()), any(UUID.class), any(MemberPrivilege.class)))
                .thenThrow(new ServiceException(FORBIDDEN, ""));
    }

    private void whenValidPrivilegeAsMember(User user, Chat chat, MemberPrivilege privilege) {
        when(memberValidator.validatePrivilegesAsMember(
                eq(user.getId()), eq(chat.getId()), eq(privilege)))
                .thenReturn(new ChatMember(user, chat,
                        Set.of(MemberPrivilege.ADD_MESSAGES, privilege)));
    }

    @Nested
    @DisplayName("readAllInMembership method")
    class ReadAllInMembership {

        @Test
        @DisplayName("throws UNAUTHORIZED when principal is not authenticated")
        void throwsUnauthorizedWhenPrincipalIsNotAuthenticated() {
            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.readAllInMembership(UUID.randomUUID()));
            assertEquals(UNAUTHORIZED, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws FORBIDDEN when principal is not a member of the chat")
        void throwsUnauthorizedWhenPrincipalIsNotMemberOfTheChat() {
            User principal = pullPrincipalUser();

            whenNotValidAsMember(principal);

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.readAllInMembership(UUID.randomUUID()));
            assertEquals(FORBIDDEN, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("returns all members when principal is a member")
        void returnsAllMembersWhenPrincipalIsMember() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);

            chat.addMember(mockUser1, privileges1);
            chat.addMember(mockUser2, privileges2);
            chat.addMember(mockUser3, privileges3);

            List<ChatMember> expectedMembersSet = List.of(
                    new ChatMember(mockUser1, chat, privileges1),
                    new ChatMember(mockUser2, chat, privileges2),
                    new ChatMember(mockUser3, chat, privileges3));

            whenValidAsMember(principal, chat);
            when(memberRepository.findAllByMembershipId(chat.getId()))
                    .thenReturn(expectedMembersSet);

            Collection<MemberPresentableView> memberPresentableViews =
                    assertDoesNotThrow(() -> memberService.readAllInMembership(chat.getId()));
            assertArrayEquals(expectedMembersSet.stream().map(MemberPresentableView::new).toArray(),
                    memberPresentableViews.toArray());
        }
    }

    @Nested
    @DisplayName("createAll method")
    class CreateAll {

        @Test
        @DisplayName("throws UNAUTHORIZED when principal is not authenticated")
        void throwsUnauthorizedWhenPrincipalIsNotAuthenticated() {
            MembersForm membersForm = new MembersForm();

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.createAll(membersForm));
            assertEquals(UNAUTHORIZED, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws UNPROCESSABLE_ENTITY when membershipId is null")
        void throwsUnprocessableEntityWhenMembershipIdIsNull() {
            MembersForm membersForm = new MembersForm();
            membersForm.setMembers(Map.of(mockUser1.getId(), EnumSet.copyOf(privileges1),
                    mockUser2.getId(), EnumSet.copyOf(privileges2)));

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.createAll(membersForm));
            assertEquals(UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws UNPROCESSABLE_ENTITY when members is null")
        void throwsUnprocessableEntityWhenMembersIsNull() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            MembersForm membersForm = new MembersForm();
            membersForm.setMembershipId(chat.getId());

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.createAll(membersForm));
            assertEquals(UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws UNPROCESSABLE_ENTITY when members is empty")
        void throwsUnprocessableEntityWhenMembersIsEmpty() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            MembersForm membersForm = new MembersForm();
            membersForm.setMembershipId(chat.getId());
            membersForm.setMembers(Map.of());

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.createAll(membersForm));
            assertEquals(UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws FORBIDDEN when principal is not a member of the chat")
        void throwsUnauthorizedWhenPrincipalIsNotMemberOfTheChat() {
            User principal = pullPrincipalUser();
            MembersForm membersForm = new MembersForm();
            membersForm.setMembershipId(UUID.randomUUID());
            membersForm.setMembers(Map.of(mockUser1.getId(), EnumSet.copyOf(privileges1)));

            whenNotValidPrivilegeAsMember(principal);

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.createAll(membersForm));
            assertEquals(FORBIDDEN, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws FORBIDDEN when principal is a member without MANAGE_MEMBERS privilege")
        void throwsForbiddenWhenPrincipalIsMemberWithoutManageMembersPrivilege() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            MembersForm membersForm = new MembersForm();
            membersForm.setMembershipId(chat.getId());
            membersForm.setMembers(Map.of(mockUser1.getId(), EnumSet.copyOf(privileges1)));

            whenNotValidPrivilegeAsMember(principal);

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.createAll(membersForm));
            assertEquals(FORBIDDEN, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws NOT_FOUND when one of the members does not exist")
        void throwsNotFoundWhenOneOfTheMembersDoesNotExist() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            MembersForm membersForm = new MembersForm();
            membersForm.setMembershipId(chat.getId());
            membersForm.setMembers(Map.of(mockUser1.getId(), EnumSet.copyOf(privileges1),
                    mockUser2.getId(), EnumSet.copyOf(privileges2)));

            whenValidPrivilegeAsMember(principal, chat, MemberPrivilege.MANAGE_MEMBERS);
            when(userRepository.allExistByIdIn(Set.of(mockUser1.getId(), mockUser2.getId())))
                    .thenReturn(false);

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.createAll(membersForm));
            assertEquals(NOT_FOUND, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws CONFLICT when one of the members is already a member")
        void throwsConflictWhenOneOfTheMembersIsAlreadyAMember() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            MembersForm membersForm = new MembersForm();
            membersForm.setMembershipId(chat.getId());
            membersForm.setMembers(Map.of(mockUser1.getId(), EnumSet.copyOf(privileges1),
                    mockUser2.getId(), EnumSet.copyOf(privileges2)));

            whenValidPrivilegeAsMember(principal, chat, MemberPrivilege.MANAGE_MEMBERS);
            when(userRepository.allExistByIdIn(Set.of(mockUser1.getId(), mockUser2.getId())))
                    .thenReturn(true);
            when(memberRepository.anyExists(Set.of(mockUser1.getId(), mockUser2.getId()), chat.getId()))
                    .thenReturn(true);

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.createAll(membersForm));
            assertEquals(CONFLICT, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws INTERNAL_SERVER_ERROR when chat could not be found")
        void throwsInternalServerErrorWhenChatCouldNotBeFound() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            MembersForm membersForm = new MembersForm();
            membersForm.setMembershipId(chat.getId());
            membersForm.setMembers(Map.of(mockUser1.getId(), EnumSet.copyOf(privileges1),
                    mockUser2.getId(), EnumSet.copyOf(privileges2)));

            whenValidPrivilegeAsMember(principal, chat, MemberPrivilege.MANAGE_MEMBERS);

            when(userRepository.allExistByIdIn(Set.of(mockUser1.getId(), mockUser2.getId())))
                    .thenReturn(true);
            when(memberRepository.anyExists(Set.of(mockUser1.getId(), mockUser2.getId()), chat.getId()))
                    .thenReturn(false);
            when(chatRepository.findById(chat.getId()))
                    .thenReturn(Optional.empty());

            UnexpectedStateException exception =
                    assertThrows(UnexpectedStateException.class, () -> memberService.createAll(membersForm));
            assertEquals(INTERNAL_SERVER_ERROR, exception.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("creates new members and saves them")
        void createsNewMembersAndSavesThem() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            MembersForm membersForm = new MembersForm();
            membersForm.setMembershipId(chat.getId());
            membersForm.setMembers(Map.of(mockUser1.getId(), EnumSet.copyOf(privileges1),
                    mockUser2.getId(), EnumSet.copyOf(privileges2)));

            whenValidPrivilegeAsMember(principal, chat, MemberPrivilege.MANAGE_MEMBERS);
            when(userRepository.allExistByIdIn(Set.of(mockUser1.getId(), mockUser2.getId())))
                    .thenReturn(true);
            when(memberRepository.anyExists(Set.of(mockUser1.getId(), mockUser2.getId()), chat.getId()))
                    .thenReturn(false);
            when(chatRepository.findById(chat.getId()))
                    .thenReturn(Optional.of(chat));

            assertDoesNotThrow(() -> memberService.createAll(membersForm));

            verify(userRepository).allExistByIdIn(anySet());
            verify(memberRepository).anyExists(anySet(), eq(chat.getId()));
            ArgumentCaptor<Collection<ChatMember>> argument = ArgumentCaptor.forClass(Collection.class);
            verify(memberRepository).saveAll(argument.capture());
            assertTrue(argument.getValue().stream()
                    .map(ChatMember::getUser)
                    .map(User::getId)
                    .collect(Collectors.toSet())
                    .containsAll(Set.of(mockUser1.getId(), mockUser2.getId())));
            assertTrue(argument.getValue().stream()
                    .map(ChatMember::getPrivileges)
                    .toList()
                    .containsAll(List.of(EnumSet.copyOf(privileges1), EnumSet.copyOf(privileges2))));
        }

        @Test
        @WithMockCustomUser
        @DisplayName("notifies all users about new members")
        void notifiesAllUsersAboutNewMembers() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            MembersForm membersForm = new MembersForm();
            membersForm.setMembershipId(chat.getId());
            membersForm.setMembers(Map.of(mockUser1.getId(), EnumSet.copyOf(privileges1),
                    mockUser2.getId(), EnumSet.copyOf(privileges2)));

            whenValidPrivilegeAsMember(principal, chat, MemberPrivilege.MANAGE_MEMBERS);
            when(userRepository.allExistByIdIn(Set.of(mockUser1.getId(), mockUser2.getId())))
                    .thenReturn(true);
            when(memberRepository.anyExists(Set.of(mockUser1.getId(), mockUser2.getId()), chat.getId()))
                    .thenReturn(false);
            when(chatRepository.findById(chat.getId()))
                    .thenReturn(Optional.of(chat));

            assertDoesNotThrow(() -> memberService.createAll(membersForm));

            ArgumentCaptor<NotificationView> argument = ArgumentCaptor.forClass(NotificationView.class);
            verify(notificationTemplate).notifyAllUsers(
                    eq(Set.of(mockUser1.getId(), mockUser2.getId())), eq(Topic.MEMBERSHIPS), argument.capture());
            assertEquals("NEW", argument.getValue().action());
        }
    }

    @Nested
    @DisplayName("updateAll method")
    class UpdateAll {

        @Test
        @DisplayName("throws UNAUTHORIZED when principal is not authenticated")
        void throwsUnauthorizedWhenPrincipalIsNotAuthenticated() {
            MembersForm membersForm = new MembersForm();

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.updateAll(membersForm));
            assertEquals(UNAUTHORIZED, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws UNPROCESSABLE_ENTITY when membershipId is null")
        void throwsUnprocessableEntityWhenMembershipIdIsNull() {
            MembersForm membersForm = new MembersForm();
            membersForm.setMembers(Map.of(mockUser1.getId(), EnumSet.copyOf(privileges1),
                    mockUser2.getId(), EnumSet.copyOf(privileges2)));

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.updateAll(membersForm));
            assertEquals(UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws UNPROCESSABLE_ENTITY when members is null")
        void throwsUnprocessableEntityWhenMembersIsNull() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            MembersForm membersForm = new MembersForm();
            membersForm.setMembershipId(chat.getId());

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.updateAll(membersForm));
            assertEquals(UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws UNPROCESSABLE_ENTITY when members is empty")
        void throwsUnprocessableEntityWhenMembersIsEmpty() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            MembersForm membersForm = new MembersForm();
            membersForm.setMembershipId(chat.getId());
            membersForm.setMembers(Map.of());

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.updateAll(membersForm));
            assertEquals(UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws FORBIDDEN when principal is not a member of the chat")
        void throwsUnauthorizedWhenPrincipalIsNotMemberOfTheChat() {
            User principal = pullPrincipalUser();
            MembersForm membersForm = new MembersForm();
            membersForm.setMembershipId(UUID.randomUUID());
            membersForm.setMembers(Map.of(mockUser1.getId(),
                    EnumSet.copyOf(privileges1)));

            whenNotValidPrivilegeAsMember(principal);

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.updateAll(membersForm));
            assertEquals(FORBIDDEN, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws FORBIDDEN when principal is a member without MANAGE_MEMBERS privilege")
        void throwsForbiddenWhenPrincipalIsMemberWithoutManageMembersPrivilege() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            MembersForm membersForm = new MembersForm();
            membersForm.setMembershipId(chat.getId());
            membersForm.setMembers(Map.of(mockUser1.getId(), EnumSet.copyOf(privileges1)));

            whenNotValidPrivilegeAsMember(principal);

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.updateAll(membersForm));
            assertEquals(FORBIDDEN, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws NOT_FOUND when one of the members does not exist")
        void throwsNotFoundWhenOneOfTheMembersDoesNotExist() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            MembersForm membersForm = new MembersForm();
            membersForm.setMembershipId(chat.getId());
            membersForm.setMembers(Map.of(mockUser1.getId(), EnumSet.copyOf(privileges1),
                    mockUser2.getId(), EnumSet.copyOf(privileges2)));

            whenValidPrivilegeAsMember(principal, chat, MemberPrivilege.MANAGE_MEMBERS);
            when(memberRepository.findAllByUserIdIn(Set.of(mockUser1.getId(), mockUser2.getId()), chat.getId()))
                    .thenReturn(List.of(new ChatMember(mockUser1, chat, privileges1)));

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.updateAll(membersForm));
            assertEquals(NOT_FOUND, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws CONFLICT when one of the members is locked")
        void throwsConflictWhenOneOfTheMembersIsNotAMember() {
            User principal = pullPrincipalUser();
            ChatMember lockedPrincipalMember =
                    new ChatMember(principal, Chat.mockOf(UUID.randomUUID()), Chat.ADMIN_PRIVILEGES);
            lockedPrincipalMember.setLocked(true);
            Chat chat = new Chat(principal);
            MembersForm membersForm = new MembersForm();
            membersForm.setMembershipId(chat.getId());
            membersForm.setMembers(Map.of(principal.getId(), EnumSet.copyOf(privileges1),
                    mockUser2.getId(), EnumSet.copyOf(privileges2)));

            whenValidPrivilegeAsMember(principal, chat, MemberPrivilege.MANAGE_MEMBERS);
            when(memberRepository.findAllByUserIdIn(Set.of(principal.getId(), mockUser2.getId()), chat.getId()))
                    .thenReturn(List.of(lockedPrincipalMember,
                            new ChatMember(mockUser2, chat, privileges2)));

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.updateAll(membersForm));
            assertEquals(CONFLICT, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("updates members and saves them")
        void updatesMembersAndSavesThem() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            MembersForm membersForm = new MembersForm();
            membersForm.setMembershipId(chat.getId());
            membersForm.setMembers(Map.of(mockUser1.getId(), EnumSet.copyOf(privileges1),
                    mockUser2.getId(), EnumSet.copyOf(privileges2)));

            whenValidPrivilegeAsMember(principal, chat, MemberPrivilege.MANAGE_MEMBERS);
            when(memberRepository.findAllByUserIdIn(Set.of(mockUser1.getId(), mockUser2.getId()), chat.getId()))
                    .thenReturn(List.of(new ChatMember(mockUser1, chat, privileges1),
                            new ChatMember(mockUser2, chat, privileges2)));

            assertDoesNotThrow(() -> memberService.updateAll(membersForm));

            ArgumentCaptor<Collection<ChatMember>> argument = ArgumentCaptor.forClass(Collection.class);
            verify(memberRepository).saveAll(argument.capture());
            assertTrue(argument.getValue().stream()
                    .map(ChatMember::getUser)
                    .map(User::getId)
                    .collect(Collectors.toSet())
                    .containsAll(Set.of(mockUser1.getId(), mockUser2.getId())));
            assertTrue(argument.getValue().stream()
                    .map(ChatMember::getPrivileges)
                    .toList()
                    .containsAll(List.of(EnumSet.copyOf(privileges1), EnumSet.copyOf(privileges2))));
        }
    }

    @Nested
    @DisplayName("delete method")
    class Delete {

        @Test
        @DisplayName("throws UNAUTHORIZED when principal is not authenticated")
        void throwsUnauthorizedWhenPrincipalIsNotAuthenticated() {
            MembersForm membersForm = new MembersForm();

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.delete(membersForm));
            assertEquals(UNAUTHORIZED, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws UNPROCESSABLE_ENTITY when membershipId is null")
        void throwsUnprocessableEntityWhenMembershipIdIsNull() {
            MembersForm membersForm = new MembersForm();
            membersForm.setMembers(Map.of(mockUser1.getId(), EnumSet.copyOf(privileges1),
                    mockUser2.getId(), EnumSet.copyOf(privileges2)));

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.delete(membersForm));
            assertEquals(UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws UNPROCESSABLE_ENTITY when members is null")
        void throwsUnprocessableEntityWhenMembersIsNull() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            MembersForm membersForm = new MembersForm();
            membersForm.setMembershipId(chat.getId());

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.delete(membersForm));
            assertEquals(UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws UNPROCESSABLE_ENTITY when members is empty")
        void throwsUnprocessableEntityWhenMembersIsEmpty() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            MembersForm membersForm = new MembersForm();
            membersForm.setMembershipId(chat.getId());
            membersForm.setMembers(Map.of());

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.delete(membersForm));
            assertEquals(UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws FORBIDDEN when principal is not a member or without MANAGE_MEMBERS privilege")
        void throwsForbiddenWhenPrincipalIsNotMemberOrWithoutManageMembersPrivilege() {
            User principal = pullPrincipalUser();
            MembersForm membersForm = new MembersForm();
            membersForm.setMembershipId(UUID.randomUUID());
            membersForm.setMembers(Map.of(mockUser1.getId(), EnumSet.copyOf(privileges1)));

            whenNotValidPrivilegeAsMember(principal);

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.delete(membersForm));
            assertEquals(FORBIDDEN, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws NOT_FOUND when one of the members does not exist")
        void throwsNotFoundWhenOneOfTheMembersDoesNotExist() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            MembersForm membersForm = new MembersForm();
            membersForm.setMembershipId(chat.getId());
            membersForm.setMembers(Map.of(mockUser1.getId(), EnumSet.copyOf(privileges1),
                    mockUser2.getId(), EnumSet.copyOf(privileges2)));

            whenValidPrivilegeAsMember(principal, chat
                    , MemberPrivilege.MANAGE_MEMBERS);
            when(memberRepository.findAllByUserIdIn(Set.of(mockUser1.getId(), mockUser2.getId()), chat.getId()))
                    .thenReturn(List.of(new ChatMember(mockUser1, chat, privileges1)));

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.delete(membersForm));
            assertEquals(NOT_FOUND, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws CONFLICT when one of the members is locked")
        void throwsConflictWhenOneOfTheMembersIsLocked() {
            User principal = pullPrincipalUser();
            ChatMember lockedPrincipalMember =
                    new ChatMember(principal, Chat.mockOf(UUID.randomUUID()), Chat.ADMIN_PRIVILEGES);
            lockedPrincipalMember.setLocked(true);
            Chat chat = new Chat(principal);
            MembersForm membersForm = new MembersForm();
            membersForm.setMembershipId(chat.getId());
            membersForm.setMembers(Map.of(principal.getId(), EnumSet.copyOf(privileges1),
                    mockUser2.getId(), EnumSet.copyOf(privileges2)));

            whenValidPrivilegeAsMember(principal, chat, MemberPrivilege.MANAGE_MEMBERS);
            when(memberRepository.findAllByUserIdIn(Set.of(principal.getId(), mockUser2.getId()), chat.getId()))
                    .thenReturn(List.of(lockedPrincipalMember,
                            new ChatMember(mockUser2, chat, privileges2)));

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> memberService.delete(membersForm));
            assertEquals(CONFLICT, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("deletes members and saves them")
        void deletesMembersAndSavesThem() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            MembersForm membersForm = new MembersForm();
            membersForm.setMembershipId(chat.getId());
            membersForm.setMembers(Map.of(mockUser1.getId(), EnumSet.copyOf(privileges1),
                    mockUser2.getId(), EnumSet.copyOf(privileges2)));

            whenValidPrivilegeAsMember(principal, chat
                    , MemberPrivilege.MANAGE_MEMBERS);
            when(memberRepository.findAllByUserIdIn(Set.of(mockUser1.getId(), mockUser2.getId()), chat.getId()))
                    .thenReturn(List.of(new ChatMember(mockUser1, chat, privileges1),
                            new ChatMember(mockUser2, chat, privileges2)));

            assertDoesNotThrow(() -> memberService.delete(membersForm));

            ArgumentCaptor<Collection<ChatMember>> argument = ArgumentCaptor.forClass(Collection.class);
            verify(memberRepository).deleteAll(argument.capture());
            assertTrue(argument.getValue().stream()
                    .map(ChatMember::getUser)
                    .map(User::getId)
                    .collect(Collectors.toSet())
                    .containsAll(Set.of(mockUser1.getId(), mockUser2.getId())));

            verify(notificationTemplate).notifyAllUsers(
                    eq(Set.of(mockUser1.getId(), mockUser2.getId())), eq(Topic.MEMBERSHIPS),
                    any(NotificationView.class));
        }
    }
}
