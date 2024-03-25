package pl.mwasyluk.ouroom_server.services.sendable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
import pl.mwasyluk.ouroom_server.domain.sendable.ChatSendable;
import pl.mwasyluk.ouroom_server.domain.user.User;
import pl.mwasyluk.ouroom_server.domain.user.UserAuthority;
import pl.mwasyluk.ouroom_server.dto.notification.NotificationView;
import pl.mwasyluk.ouroom_server.dto.sendable.SendableForm;
import pl.mwasyluk.ouroom_server.dto.sendable.SendableView;
import pl.mwasyluk.ouroom_server.exceptions.ServiceException;
import pl.mwasyluk.ouroom_server.mocks.WithMockCustomUser;
import pl.mwasyluk.ouroom_server.repos.SendableRepository;
import pl.mwasyluk.ouroom_server.services.MemberValidator;
import pl.mwasyluk.ouroom_server.websocket.NotificationTemplate;
import pl.mwasyluk.ouroom_server.websocket.Topic;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static pl.mwasyluk.ouroom_server.mocks.WithUserDetailsSecurityContextFactory.pullPrincipalUser;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig
class DefaultSendableServiceTest {
    @Mock
    private SendableRepository sendableRepository;
    @Mock
    private MemberValidator memberValidator;
    @Mock
    private NotificationTemplate notificationTemplate;

    private DefaultSendableService sendableService;
    private User mockUser1;
    private ChatSendable mockSendable1;
    private ChatSendable mockSendable2;
    private ChatSendable mockSendable3;

    @BeforeEach
    void setUp() {
        sendableService = new DefaultSendableService(sendableRepository, memberValidator, notificationTemplate);
        mockUser1 = new User("u1", "pass", Set.of(UserAuthority.USER));

        mockSendable1 = new ChatSendable(mockUser1, "m1");
        mockSendable2 = new ChatSendable(mockUser1, "m2");
        mockSendable3 = new ChatSendable(mockUser1, "m3");
        mockSendable1.setContainer(new Chat(mockUser1));
        mockSendable2.setContainer(new Chat(mockUser1));
        mockSendable3.setContainer(new Chat(mockUser1));
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
                        Set.of(privilege)));
    }

    @Nested
    @DisplayName("readAllFromContainer method")
    class ReadAllFromContainer {
        // provide test methods similar to the ones in DefaultMemberServiceTest

        @Test
        @DisplayName("throws UNAUTHORIZED when user is not authenticated")
        void throwsUnauthorizedWhenUserIsNotAuthenticated() {
            ServiceException serviceException = assertThrowsExactly(ServiceException.class,
                    () -> sendableService.readAllFromContainer(UUID.randomUUID()));
            assertEquals(UNAUTHORIZED, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws FORBIDDEN when user is not a member")
        void throwsForbiddenWhenUserIsNotAMember() {
            User principal = pullPrincipalUser();
            whenNotValidAsMember(principal);

            ServiceException serviceException = assertThrowsExactly(ServiceException.class,
                    () -> sendableService.readAllFromContainer(UUID.randomUUID()));
            assertEquals(FORBIDDEN, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("returns a list of SendableViews when user is a member")
        void returnsListOfSendableViewsWhenUserIsAMember() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);

            whenValidAsMember(principal, chat);
            when(sendableRepository.findAllByContainerId(chat.getId()))
                    .thenReturn(List.of(mockSendable1, mockSendable2, mockSendable3));

            Collection<SendableView> sendableViews =
                    assertDoesNotThrow(() -> sendableService.readAllFromContainer(chat.getId()));
            assertEquals(3, sendableViews.size());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("returns an empty list when there are no sendables")
        void returnsEmptyListWhenThereAreNoSendables() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);

            whenValidAsMember(principal, chat);
            when(sendableRepository.findAllByContainerId(chat.getId()))
                    .thenReturn(List.of());

            Collection<SendableView> sendableViews =
                    assertDoesNotThrow(() -> sendableService.readAllFromContainer(chat.getId()));
            assertTrue(sendableViews.isEmpty());
        }
    }

    @Nested
    @DisplayName("create method")
    class Create {
        @Test
        @DisplayName("throws UNAUTHORIZED when user is not authenticated")
        void throwsUnauthorizedWhenUserIsNotAuthenticated() {
            SendableForm form = new SendableForm();

            ServiceException serviceException = assertThrowsExactly(ServiceException.class,
                    () -> sendableService.create(form));
            assertEquals(UNAUTHORIZED, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws UNPROCESSABLE_ENTITY when container ID is null")
        void throwsUnprocessableEntityWhenContainerIdIsNull() {
            SendableForm form = new SendableForm();
            form.setMessage("test");

            ServiceException serviceException = assertThrowsExactly(ServiceException.class,
                    () -> sendableService.create(form));
            assertEquals(UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws UNPROCESSABLE_ENTITY when message is null")
        void throwsUnprocessableEntityWhenMessageIsNull() {
            SendableForm form = new SendableForm();
            form.setContainerId(UUID.randomUUID());

            ServiceException serviceException = assertThrowsExactly(ServiceException.class,
                    () -> sendableService.create(form));
            assertEquals(UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws UNPROCESSABLE_ENTITY when message is blank")
        void throwsUnprocessableEntityWhenMessageIsBlank() {
            SendableForm form = new SendableForm();
            form.setContainerId(UUID.randomUUID());
            form.setMessage(" \n   \t ");

            ServiceException serviceException = assertThrowsExactly(ServiceException.class,
                    () -> sendableService.create(form));
            assertEquals(UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws FORBIDDEN when user is not a member or does not have ADD_MESSAGES privilege")
        void throwsForbiddenWhenUserIsNotAMember() {
            User principal = pullPrincipalUser();
            SendableForm form = new SendableForm();
            form.setContainerId(UUID.randomUUID());
            form.setMessage("test");

            whenNotValidPrivilegeAsMember(principal);

            ServiceException serviceException = assertThrowsExactly(ServiceException.class,
                    () -> sendableService.create(form));
            assertEquals(FORBIDDEN, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("creates a valid sendable when user is a member with ADD_MESSAGES privilege")
        void createsValidSendableWhenUserIsAMemberWithAddMessagesPrivilege() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            SendableForm form = new SendableForm();
            form.setContainerId(chat.getId());
            form.setMessage("\n test \t  ");

            whenValidPrivilegeAsMember(principal, chat, MemberPrivilege.ADD_MESSAGES);
            when(sendableRepository.save(any(ChatSendable.class)))
                    .thenReturn(mockSendable1);

            SendableView sendableView = assertDoesNotThrow(() ->
                    sendableService.create(form));
            assertNotNull(sendableView);

            ArgumentCaptor<ChatSendable> argument = ArgumentCaptor.forClass(ChatSendable.class);
            verify(sendableRepository).save(argument.capture());
            assertAll(() -> {
                assertEquals(principal, argument.getValue().getCreator());
                assertEquals("test", argument.getValue().getMessage());
                assertEquals(chat, argument.getValue().getContainer());
            });
        }

        @Test
        @WithMockCustomUser
        @DisplayName("notifies all members when a new sendable is created")
        void notifiesAllMembersWhenNewSendableIsCreated() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            SendableForm form = new SendableForm();
            form.setContainerId(chat.getId());
            form.setMessage("test");

            whenValidPrivilegeAsMember(principal, chat, MemberPrivilege.ADD_MESSAGES);
            when(sendableRepository.save(any(ChatSendable.class)))
                    .thenReturn(mockSendable1);

            assertDoesNotThrow(() -> sendableService.create(form));
            ArgumentCaptor<NotificationView> argument = ArgumentCaptor.forClass(NotificationView.class);
            verify(notificationTemplate).notifyAllMembers(eq(chat.getId()), eq(Topic.MESSAGES), argument.capture());
            assertEquals("NEW", argument.getValue().action());
        }
    }

    @Nested
    @DisplayName("update method")
    class Update {
        @Test
        @DisplayName("throws UNAUTHORIZED when user is not authenticated")
        void throwsUnauthorizedWhenUserIsNotAuthenticated() {
            SendableForm form = new SendableForm();

            ServiceException serviceException = assertThrowsExactly(ServiceException.class,
                    () -> sendableService.update(form));
            assertEquals(UNAUTHORIZED, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws UNPROCESSABLE_ENTITY when sendable ID is null")
        void throwsUnprocessableEntityWhenSendableIdIsNull() {
            SendableForm form = new SendableForm();
            form.setMessage("test");

            ServiceException serviceException = assertThrowsExactly(ServiceException.class,
                    () -> sendableService.update(form));
            assertEquals(UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws NOT_FOUND when the sendable does not exist")
        void throwsNotFoundWhenTheSendableDoesNotExist() {
            SendableForm form = new SendableForm();
            form.setSendableId(UUID.randomUUID());
            form.setMessage("test");

            when(sendableRepository.findById(form.getSendableId()))
                    .thenReturn(Optional.empty());

            ServiceException serviceException = assertThrowsExactly(ServiceException.class,
                    () -> sendableService.update(form));
            assertEquals(NOT_FOUND, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws FORBIDDEN when user is not the creator of the sendable")
        void throwsForbiddenWhenUserIsNotTheCreatorOfTheSendable() {
            SendableForm form = new SendableForm();
            form.setSendableId(UUID.randomUUID());
            form.setMessage("test");

            when(sendableRepository.findById(form.getSendableId()))
                    .thenReturn(Optional.of(mockSendable1));

            ServiceException serviceException = assertThrowsExactly(ServiceException.class,
                    () -> sendableService.update(form));
            assertEquals(FORBIDDEN, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws FORBIDDEN when principal is chat owner but not sendable creator")
        void throwsForbiddenWhenPrincipalIsChatOwnerButNotSendableCreator() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            ChatSendable sendable = new ChatSendable(mockUser1, "message");
            sendable.setContainer(chat);
            SendableForm form = new SendableForm();
            form.setSendableId(sendable.getId());
            form.setMessage("test");

            when(sendableRepository.findById(sendable.getId()))
                    .thenReturn(Optional.of(sendable));

            ServiceException serviceException = assertThrowsExactly(ServiceException.class,
                    () -> sendableService.update(form));
            assertEquals(FORBIDDEN, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws UNPROCESSABLE_ENTITY when the new content cannot be applied to the sendable")
        void throwsUnprocessableEntityWhenNewContentCannotBeAppliedToTheSendable() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            ChatSendable sendable = new ChatSendable(principal, "message");
            sendable.setContainer(chat);
            SendableForm form = new SendableForm();
            form.setSendableId(sendable.getId());
            form.setMessage(" \n   \t ");

            when(sendableRepository.findById(sendable.getId()))
                    .thenReturn(Optional.of(sendable));

            ServiceException serviceException = assertThrowsExactly(ServiceException.class,
                    () -> sendableService.update(form));
            assertEquals(UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("updates sendable when user is the creator of the sendable and the new content is valid")
        void updatesSendableWhenUserIsTheCreatorOfTheSendableAndTheNewContentIsValid() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            ChatSendable sendable = new ChatSendable(principal, "message");
            sendable.setContainer(chat);
            SendableForm form = new SendableForm();
            form.setSendableId(sendable.getId());
            form.setMessage("test \n  \t");

            when(sendableRepository.findById(sendable.getId()))
                    .thenReturn(Optional.of(sendable));
            when(sendableRepository.save(any(ChatSendable.class))).thenReturn(sendable);

            SendableView sendableView = assertDoesNotThrow(() ->
                    sendableService.update(form));
            assertNotNull(sendableView);

            ArgumentCaptor<ChatSendable> argument = ArgumentCaptor.forClass(ChatSendable.class);
            verify(sendableRepository).save(argument.capture());
            assertAll(() -> {
                assertEquals(principal, argument.getValue().getCreator());
                assertEquals("test", argument.getValue().getMessage());
                assertEquals(chat, argument.getValue().getContainer());
            });
        }

        @Test
        @WithMockCustomUser
        @DisplayName("notifies all members when a sendable is updated")
        void notifiesAllMembersWhenSendableIsUpdated() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            ChatSendable sendable = new ChatSendable(principal, "message");
            sendable.setContainer(chat);
            SendableForm form = new SendableForm();
            form.setSendableId(UUID.randomUUID());
            form.setMessage("test");

            when(sendableRepository.findById(form.getSendableId()))
                    .thenReturn(Optional.of(sendable));
            when(sendableRepository.save(any(ChatSendable.class))).thenReturn(sendable);

            assertDoesNotThrow(() -> sendableService.update(form));
            ArgumentCaptor<NotificationView> argument = ArgumentCaptor.forClass(NotificationView.class);
            verify(notificationTemplate).notifyAllMembers(eq(chat.getId()), eq(Topic.MESSAGES), argument.capture());
            assertEquals("CHANGED", argument.getValue().action());
        }
    }

    @Nested
    @DisplayName("delete method")
    class Delete {
        @Test
        @DisplayName("throws UNAUTHORIZED when user is not authenticated")
        void throwsUnauthorizedWhenUserIsNotAuthenticated() {
            UUID sendableId = UUID.randomUUID();

            ServiceException serviceException = assertThrowsExactly(ServiceException.class,
                    () -> sendableService.delete(sendableId));
            assertEquals(UNAUTHORIZED, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws NOT_FOUND when the sendable does not exist")
        void throwsNotFoundWhenTheSendableDoesNotExist() {
            UUID sendableId = UUID.randomUUID();

            when(sendableRepository.findById(sendableId))
                    .thenReturn(Optional.empty());

            ServiceException serviceException = assertThrowsExactly(ServiceException.class,
                    () -> sendableService.delete(sendableId));
            assertEquals(NOT_FOUND, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws FORBIDDEN when user is not creator and does not have DELETE_MESSAGES privilege")
        void throwsForbiddenWhenUserIsNotCreatorAndDoesNotHaveDeleteMessagesPrivilege() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            ChatSendable sendable = new ChatSendable(mockUser1, "message");
            sendable.setContainer(chat);
            UUID sendableId = sendable.getId();

            when(sendableRepository.findById(sendableId))
                    .thenReturn(Optional.of(sendable));
            whenNotValidPrivilegeAsMember(principal);

            ServiceException serviceException = assertThrowsExactly(ServiceException.class,
                    () -> sendableService.delete(sendableId));
            assertEquals(FORBIDDEN, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("deletes sendable when user is the creator of the sendable")
        void deletesSendableWhenUserIsTheCreatorOfTheSendable() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            ChatSendable sendable = new ChatSendable(principal, "message");
            sendable.setContainer(chat);
            UUID sendableId = sendable.getId();

            when(sendableRepository.findById(sendableId))
                    .thenReturn(Optional.of(sendable));

            assertDoesNotThrow(() -> sendableService.delete(sendableId));
            verify(sendableRepository).deleteById(sendableId);
        }

        @Test
        @WithMockCustomUser
        @DisplayName("deletes sendable when user is not the creator but has DELETE_MESSAGES privilege")
        void deletesSendableWhenUserIsNotTheCreatorButHasDeleteMessagesPrivilege() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            ChatSendable sendable = new ChatSendable(mockUser1, "message");
            sendable.setContainer(chat);
            UUID sendableId = sendable.getId();

            when(sendableRepository.findById(sendableId))
                    .thenReturn(Optional.of(sendable));
            whenValidPrivilegeAsMember(principal, chat, MemberPrivilege.DELETE_MESSAGES);

            assertDoesNotThrow(() -> sendableService.delete(sendableId));
            verify(sendableRepository).deleteById(sendableId);
        }

        @Test
        @WithMockCustomUser
        @DisplayName("notifies all members when a sendable is deleted")
        void notifiesAllMembersWhenSendableIsDeleted() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            ChatSendable sendable = new ChatSendable(principal, "message");
            sendable.setContainer(chat);
            UUID sendableId = sendable.getId();

            when(sendableRepository.findById(sendableId))
                    .thenReturn(Optional.of(sendable));

            assertDoesNotThrow(() -> sendableService.delete(sendableId));
            ArgumentCaptor<NotificationView> argument = ArgumentCaptor.forClass(NotificationView.class);
            verify(notificationTemplate).notifyAllMembers(eq(chat.getId()), eq(Topic.MESSAGES), argument.capture());
            assertEquals("REMOVED", argument.getValue().action());
        }
    }
}
