package pl.mwasyluk.ouroom_server.services.chat;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
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
import pl.mwasyluk.ouroom_server.domain.media.Image;
import pl.mwasyluk.ouroom_server.domain.media.Media;
import pl.mwasyluk.ouroom_server.domain.media.source.DataSource;
import pl.mwasyluk.ouroom_server.domain.media.source.DataSourceTestUtil;
import pl.mwasyluk.ouroom_server.domain.member.ChatMember;
import pl.mwasyluk.ouroom_server.domain.member.MemberPrivilege;
import pl.mwasyluk.ouroom_server.domain.user.User;
import pl.mwasyluk.ouroom_server.domain.user.UserAuthority;
import pl.mwasyluk.ouroom_server.dto.chat.ChatDetailsView;
import pl.mwasyluk.ouroom_server.dto.chat.ChatForm;
import pl.mwasyluk.ouroom_server.dto.chat.ChatPresentableView;
import pl.mwasyluk.ouroom_server.dto.notification.NotificationView;
import pl.mwasyluk.ouroom_server.exceptions.ServiceException;
import pl.mwasyluk.ouroom_server.exceptions.UnexpectedStateException;
import pl.mwasyluk.ouroom_server.mocks.WithMockCustomUser;
import pl.mwasyluk.ouroom_server.repos.ChatRepository;
import pl.mwasyluk.ouroom_server.services.MemberValidator;
import pl.mwasyluk.ouroom_server.websocket.NotificationTemplate;
import pl.mwasyluk.ouroom_server.websocket.Topic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pl.mwasyluk.ouroom_server.mocks.WithUserDetailsSecurityContextFactory.pullPrincipalUser;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig
class DefaultChatServiceTest {
    @Mock
    private ChatRepository chatRepository;
    @Mock
    private MemberValidator memberValidator;
    @Mock
    private NotificationTemplate notificationTemplate;

    private DefaultChatService defaultChatService;
    private User mockUser;
    private Image mockJpegImage;
    private MockMultipartFile mockPngImageFile;

    @BeforeEach
    void setUp() {
        defaultChatService = new DefaultChatService(chatRepository, memberValidator, notificationTemplate);
        mockUser = new User("test", "pass", Set.of(UserAuthority.USER));
        mockJpegImage = (Image) Media.of(DataSource.of(DataSourceTestUtil.JPEG_BYTES));
        mockPngImageFile = new MockMultipartFile("mock_image.png", DataSourceTestUtil.PNG_BYTES);
    }

    private void whenUserIsAMemberWithManageDetails(User user, Chat chat) {
        when(memberValidator.validateAsMember(user.getId(), chat.getId()))
                .thenReturn(new ChatMember(user, chat, Set.of(MemberPrivilege.MANAGE_DETAILS)));
    }

    private void whenUserIsAMemberWithoutManageDetails(User user, Chat chat) {
        when(memberValidator.validateAsMember(user.getId(), chat.getId()))
                .thenReturn(new ChatMember(user, chat, Set.of()));
    }

    @Nested
    @DisplayName("create method")
    class CreateMethod {
        @Test
        @DisplayName("throws UNAUTHORIZED when user is not authenticated")
        void throwsUnauthorizedWhenUserIsNotAuthenticated() {
            ChatForm chatForm = new ChatForm();

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> defaultChatService.create(chatForm));
            assertEquals(HttpStatus.UNAUTHORIZED, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("returns ChatPresentableView when user is authenticated and empty form provided")
        void returnsChatPresentableViewWhenUserIsAuthenticatedAndEmptyFormProvided() {
            User principal = pullPrincipalUser();
            ChatForm chatForm = new ChatForm();

            when(chatRepository.save(any())).thenReturn(new Chat(principal));

            ChatPresentableView chatPresentableView = assertDoesNotThrow(() -> defaultChatService.create(chatForm));
            assertNotNull(chatPresentableView);
        }

        @Test
        @WithMockCustomUser
        @DisplayName("sets chat name to null when blank name provided")
        void setsChatNameToNullWhenBlankNameProvided() {
            User principal = pullPrincipalUser();
            ChatForm chatForm = new ChatForm();
            chatForm.setName("  \n  ");

            when(chatRepository.save(any())).thenReturn(new Chat(principal));

            assertDoesNotThrow(() -> defaultChatService.create(chatForm));

            ArgumentCaptor<Chat> argument = ArgumentCaptor.forClass(Chat.class);
            verify(chatRepository).save(argument.capture());
            assertNull(argument.getValue().getName());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("sets chat name to trimmed value when non-blank name provided")
        void setsChatNameToTrimmedValueWhenNonBlankNameProvided() {
            User principal = pullPrincipalUser();
            ChatForm chatForm = new ChatForm();
            chatForm.setName("  Test  ");

            when(chatRepository.save(any())).thenReturn(new Chat(principal));

            assertDoesNotThrow(() -> defaultChatService.create(chatForm));

            ArgumentCaptor<Chat> argument = ArgumentCaptor.forClass(Chat.class);
            verify(chatRepository).save(argument.capture());
            assertEquals("Test", argument.getValue().getName());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("sets chat image when valid image file provided")
        void setsChatImageWhenValidImageFileProvided() throws IOException {
            User principal = pullPrincipalUser();
            ChatForm chatForm = new ChatForm();
            chatForm.setFile(mockPngImageFile);

            when(chatRepository.save(any())).thenReturn(new Chat(principal));

            assertDoesNotThrow(() -> defaultChatService.create(chatForm));

            ArgumentCaptor<Chat> argument = ArgumentCaptor.forClass(Chat.class);
            verify(chatRepository).save(argument.capture());
            assertArrayEquals(mockPngImageFile.getBytes(), argument.getValue().getImage().getSource().getData());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws UNPROCESSABLE_ENTITY when invalid image file provided")
        void throwsUnprocessableEntityWhenInvalidImageFileProvided() {
            ChatForm chatForm = new ChatForm();
            chatForm.setFile(new MockMultipartFile("invalid_image.jpeg", "invalid".getBytes()));

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> defaultChatService.create(chatForm));
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("notifies all members about new chat")
        void notifiesAllMembersAboutNewChat() {
            User principal = pullPrincipalUser();
            ChatForm chatForm = new ChatForm();

            when(chatRepository.save(any())).thenReturn(new Chat(principal));

            assertDoesNotThrow(() -> defaultChatService.create(chatForm));

            ArgumentCaptor<NotificationView> argument = ArgumentCaptor.forClass(NotificationView.class);
            verify(notificationTemplate).notifyAllMembers(any(), eq(Topic.MEMBERSHIPS), argument.capture());
            assertEquals("NEW", argument.getValue().action());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("sets all chat properties correctly")
        void setsAllChatPropertiesCorrectly() throws IOException {
            User principal = pullPrincipalUser();
            ChatForm chatForm = new ChatForm();
            chatForm.setName("  Test \n  ");
            chatForm.setFile(mockPngImageFile);

            when(chatRepository.save(any())).thenReturn(new Chat(principal));

            assertDoesNotThrow(() -> defaultChatService.create(chatForm));

            ArgumentCaptor<Chat> argument = ArgumentCaptor.forClass(Chat.class);
            verify(chatRepository).save(argument.capture());
            assertEquals("Test", argument.getValue().getName());
            assertArrayEquals(mockPngImageFile.getBytes(), argument.getValue().getImage().getSource().getData());
        }
    }

    @Nested
    @DisplayName("readAllWithPrincipal method")
    class ReadAllWithPrincipalMethod {
        @Test
        @DisplayName("throws UNAUTHORIZED when user is not authenticated")
        void throwsUnauthorizedWhenUserIsNotAuthenticated() {
            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> defaultChatService.readAllWithPrincipal());
            assertEquals(HttpStatus.UNAUTHORIZED, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("returns empty collection when user is authenticated and has no chats")
        void returnsEmptyCollectionWhenUserIsAuthenticatedAndHasNoChats() {
            when(chatRepository.findAllByUserId(any())).thenReturn(Set.of());

            Collection<ChatPresentableView> chatPresentableViews =
                    assertDoesNotThrow(() -> defaultChatService.readAllWithPrincipal());
            assertEquals(0, chatPresentableViews.size());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("returns collection of ChatPresentableView when user is authenticated and has chats")
        void returnsCollectionOfChatPresentableViewWhenUserIsAuthenticatedAndHasChats() {
            User principal = pullPrincipalUser();
            Chat chat1 = new Chat(principal);
            Chat chat2 = new Chat(principal);
            when(chatRepository.findAllByUserId(any())).thenReturn(Set.of(chat1, chat2));

            Collection<ChatPresentableView> chatPresentableViews =
                    assertDoesNotThrow(() -> defaultChatService.readAllWithPrincipal());
            assertEquals(2, chatPresentableViews.size());
        }
    }

    @Nested
    @DisplayName("read method")
    class ReadMethod {

        @Test
        @DisplayName("throws UNAUTHORIZED when user is not authenticated")
        void throwsUnauthorizedWhenUserIsNotAuthenticated() {
            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> defaultChatService.read(UUID.randomUUID()));
            assertEquals(HttpStatus.UNAUTHORIZED, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws INTERNAL_SERVER_ERROR when chat could not be found")
        void throwsInternalServerErrorWhenChatCouldNotBeFound() {
            User principal = pullPrincipalUser();
            UUID randomUUID = UUID.randomUUID();

            when(chatRepository.findDetailsById(randomUUID)).thenReturn(Optional.empty());

            UnexpectedStateException exception =
                    assertThrows(UnexpectedStateException.class, () -> defaultChatService.read(randomUUID));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());

            verify(memberValidator).validateAsMember(principal.getId(), randomUUID);
            verify(chatRepository).findDetailsById(randomUUID);
        }

        @Test
        @WithMockCustomUser
        @DisplayName("returns ChatDetailsView when user is authenticated and is a member of the chat")
        void returnsChatDetailsViewWhenUserIsAuthenticatedAndIsAMemberOfTheChat() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(mockUser);
            chat.addMember(principal, Set.of());

            when(chatRepository.findDetailsById(chat.getId())).thenReturn(Optional.of(new ChatDetailsView(chat)));

            ChatDetailsView chatDetailsView = assertDoesNotThrow(() -> defaultChatService.read(chat.getId()));
            assertNotNull(chatDetailsView);

            verify(memberValidator).validateAsMember(principal.getId(), chat.getId());
            verify(chatRepository).findDetailsById(chat.getId());
        }
    }

    @Nested
    @DisplayName("update method")
    class UpdateMethod {

        @Test
        @DisplayName("throws UNAUTHORIZED when user is not authenticated")
        void throwsUnauthorizedWhenUserIsNotAuthenticated() {
            ChatForm chatForm = new ChatForm();

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> defaultChatService.update(chatForm));
            assertEquals(HttpStatus.UNAUTHORIZED, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws UNPROCESSABLE_ENTITY when chat ID is not provided")
        void throwsUnprocessableEntityWhenChatIdIsNotProvided() {
            ChatForm chatForm = new ChatForm();

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> defaultChatService.update(chatForm));
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws FORBIDDEN when user is authenticated but does not have MANAGE_DETAILS privilege")
        void throwsForbiddenWhenUserIsAuthenticatedButDoesNotHaveManageDetailsPrivilege() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(mockUser);
            chat.addMember(principal, Set.of());

            ChatForm chatForm = new ChatForm();
            chatForm.setChatId(chat.getId());

            whenUserIsAMemberWithoutManageDetails(principal, chat);

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> defaultChatService.update(chatForm));
            assertEquals(HttpStatus.FORBIDDEN, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws INTERNAL_SERVER_ERROR when chat could not be found")
        void throwsInternalServerErrorWhenChatCouldNotBeFound() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);

            ChatForm chatForm = new ChatForm();
            chatForm.setChatId(chat.getId());

            whenUserIsAMemberWithManageDetails(principal, chat);
            when(chatRepository.findById(chat.getId())).thenReturn(Optional.empty());

            UnexpectedStateException exception =
                    assertThrows(UnexpectedStateException.class, () -> defaultChatService.update(chatForm));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("sets chat name to null when blank name provided")
        void setsChatNameToNullWhenBlankNameProvided() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);
            chat.addMember(principal, Set.of());

            ChatForm chatForm = new ChatForm();
            chatForm.setChatId(chat.getId());
            chatForm.setName("  \n  ");

            whenUserIsAMemberWithManageDetails(principal, chat);
            when(chatRepository.findById(chatForm.getChatId())).thenReturn(Optional.of(chat));
            when(chatRepository.save(any())).thenReturn(chat);

            assertDoesNotThrow(() -> defaultChatService.update(chatForm));

            ArgumentCaptor<Chat> argument = ArgumentCaptor.forClass(Chat.class);
            verify(chatRepository).save(argument.capture());
            assertNull(argument.getValue().getName());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("sets chat name to trimmed value when non-blank name provided")
        void setsChatNameToTrimmedValueWhenNonBlankNameProvided() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);

            ChatForm chatForm = new ChatForm();
            chatForm.setChatId(chat.getId());
            chatForm.setName("  Test  ");

            whenUserIsAMemberWithManageDetails(principal, chat);
            when(chatRepository.findById(chatForm.getChatId())).thenReturn(Optional.of(chat));
            when(chatRepository.save(any())).thenReturn(chat);

            assertDoesNotThrow(() -> defaultChatService.update(chatForm));

            ArgumentCaptor<Chat> argument = ArgumentCaptor.forClass(Chat.class);
            verify(chatRepository).save(argument.capture());
            assertEquals("Test", argument.getValue().getName());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("notifies all members about changed chat")
        void notifiesAllMembersAboutChangedChat() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);

            ChatForm chatForm = new ChatForm();
            chatForm.setChatId(chat.getId());

            whenUserIsAMemberWithManageDetails(principal, chat);
            when(chatRepository.findById(chatForm.getChatId())).thenReturn(Optional.of(chat));
            when(chatRepository.save(any())).thenReturn(chat);

            assertDoesNotThrow(() -> defaultChatService.update(chatForm));

            ArgumentCaptor<NotificationView> argument = ArgumentCaptor.forClass(NotificationView.class);
            verify(notificationTemplate).notifyAllMembers(eq(chat.getId()), eq(Topic.MEMBERSHIPS), argument.capture());
            assertEquals("CHANGED", argument.getValue().action());
        }

        @Nested
        @DisplayName("for image")
        @SpringBootTest
        @ActiveProfiles("test")
        class ForImage {
            @Test
            @WithMockCustomUser
            @DisplayName("sets chat image when valid image file provided")
            void setsChatImageWhenValidImageFileProvided() throws IOException {
                User principal = pullPrincipalUser();
                Chat chat = new Chat(principal);

                ChatForm chatForm = new ChatForm();
                chatForm.setChatId(chat.getId());
                chatForm.setFile(mockPngImageFile);

                whenUserIsAMemberWithManageDetails(principal, chat);
                when(chatRepository.findById(chatForm.getChatId())).thenReturn(Optional.of(chat));
                when(chatRepository.save(any())).thenReturn(chat);

                assertDoesNotThrow(() -> defaultChatService.update(chatForm));

                ArgumentCaptor<Chat> argument = ArgumentCaptor.forClass(Chat.class);
                verify(chatRepository).save(argument.capture());
                assertArrayEquals(mockPngImageFile.getBytes(), argument.getValue().getImage().getSource().getData());
            }

            @Test
            @WithMockCustomUser
            @DisplayName("throws UNPROCESSABLE_ENTITY when invalid image file provided")
            void throwsUnprocessableEntityWhenInvalidImageFileProvided() {
                User principal = pullPrincipalUser();
                Chat chat = new Chat(principal);

                ChatForm chatForm = new ChatForm();
                chatForm.setChatId(chat.getId());
                chatForm.setFile(new MockMultipartFile("invalid_image.jpeg", "invalid".getBytes()));

                whenUserIsAMemberWithManageDetails(principal, chat);
                when(chatRepository.findById(chatForm.getChatId())).thenReturn(Optional.of(chat));

                ServiceException serviceException =
                        assertThrows(ServiceException.class, () -> defaultChatService.update(chatForm));
                assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
            }

            @Test
            @WithMockCustomUser
            @DisplayName("clears chat image when clear image flag is set")
            void clearsChatImageWhenClearImageFlagIsSet() {
                User principal = pullPrincipalUser();
                Chat chat = new Chat(principal);
                chat.setImage(mockJpegImage);

                ChatForm chatForm = new ChatForm();
                chatForm.setChatId(chat.getId());
                chatForm.setClearImage(true);

                whenUserIsAMemberWithManageDetails(principal, chat);
                when(chatRepository.findById(chatForm.getChatId())).thenReturn(Optional.of(chat));
                when(chatRepository.save(any())).thenReturn(chat);

                assertDoesNotThrow(() -> defaultChatService.update(chatForm));

                ArgumentCaptor<Chat> argument = ArgumentCaptor.forClass(Chat.class);
                verify(chatRepository).save(argument.capture());
                assertNull(argument.getValue().getImage());
            }

            @Test
            @WithMockCustomUser
            @DisplayName("overrides all chat properties correctly")
            void overridesAllChatPropertiesCorrectly() throws IOException {
                User principal = pullPrincipalUser();
                Chat chat = new Chat(principal);
                chat.setName("Old name");
                chat.setImage(mockJpegImage);

                ChatForm chatForm = new ChatForm();
                chatForm.setChatId(chat.getId());
                chatForm.setName("  Test \n  ");
                chatForm.setFile(mockPngImageFile);

                whenUserIsAMemberWithManageDetails(principal, chat);
                when(chatRepository.findById(chatForm.getChatId())).thenReturn(Optional.of(chat));
                when(chatRepository.save(any())).thenReturn(chat);

                assertDoesNotThrow(() -> defaultChatService.update(chatForm));

                ArgumentCaptor<Chat> argument = ArgumentCaptor.forClass(Chat.class);
                verify(chatRepository).save(argument.capture());
                assertEquals("Test", argument.getValue().getName());
                assertArrayEquals(mockPngImageFile.getBytes(), argument.getValue().getImage().getSource().getData());
            }
        }
    }

    @Nested
    @DisplayName("delete method")
    class DeleteMethod {

        @Test
        @DisplayName("throws NullPointerException when chat ID is null")
        void throwsNullPointerExceptionWhenChatIdIsNull() {
            assertThrows(NullPointerException.class, () -> defaultChatService.delete(null));
        }

        @Test
        @DisplayName("throws UNAUTHORIZED when user is not authenticated")
        void throwsUnauthorizedWhenUserIsNotAuthenticated() {
            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> defaultChatService.delete(UUID.randomUUID()));
            assertEquals(HttpStatus.UNAUTHORIZED, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws NOT_FOUND when chat could not be found")
        void throwsNotFoundWhenChatCouldNotBeFound() {
            when(chatRepository.findById(any())).thenReturn(Optional.empty());

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> defaultChatService.delete(UUID.randomUUID()));
            assertEquals(HttpStatus.NOT_FOUND, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws FORBIDDEN when user is not owner")
        void throwsForbiddenWhenUserIsNotOwner() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(mockUser);
            chat.addMember(principal, Set.of());

            when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> defaultChatService.delete(chat.getId()));
            assertEquals(HttpStatus.FORBIDDEN, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws FORBIDDEN when user has MANAGE_DETAILS privilege but is not owner")
        void throwsForbiddenWhenUserHasManageDetailsPrivilegeButIsNotOwner() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(mockUser);
            chat.addMember(principal, Set.of(MemberPrivilege.MANAGE_DETAILS));

            when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));

            ServiceException serviceException =
                    assertThrows(ServiceException.class, () -> defaultChatService.delete(chat.getId()));
            assertEquals(HttpStatus.FORBIDDEN, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("deletes chat when user is owner")
        void deletesChatWhenUserIsOwner() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);

            when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));

            assertDoesNotThrow(() -> defaultChatService.delete(chat.getId()));
            verify(chatRepository).deleteById(chat.getId());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("notifies all members about removed chat")
        void notifiesAllMembersAboutRemovedChat() {
            User principal = pullPrincipalUser();
            Chat chat = new Chat(principal);

            when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));

            assertDoesNotThrow(() -> defaultChatService.delete(chat.getId()));

            ArgumentCaptor<NotificationView> argument = ArgumentCaptor.forClass(NotificationView.class);
            verify(notificationTemplate).notifyAllMembers(eq(chat.getId()), eq(Topic.MEMBERSHIPS), argument.capture());
            assertEquals("REMOVED", argument.getValue().action());
        }
    }
}
