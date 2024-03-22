package pl.mwasyluk.ouroom_server.services.chat;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import pl.mwasyluk.ouroom_server.domain.container.Chat;
import pl.mwasyluk.ouroom_server.domain.media.Image;
import pl.mwasyluk.ouroom_server.domain.media.Media;
import pl.mwasyluk.ouroom_server.domain.media.source.DataSource;
import pl.mwasyluk.ouroom_server.domain.member.Member;
import pl.mwasyluk.ouroom_server.domain.member.MemberPrivilege;
import pl.mwasyluk.ouroom_server.domain.user.User;
import pl.mwasyluk.ouroom_server.dto.chat.ChatDetailsView;
import pl.mwasyluk.ouroom_server.dto.chat.ChatForm;
import pl.mwasyluk.ouroom_server.dto.chat.ChatPresentableView;
import pl.mwasyluk.ouroom_server.dto.notification.NotificationView;
import pl.mwasyluk.ouroom_server.exceptions.ServiceException;
import pl.mwasyluk.ouroom_server.exceptions.UnexpectedStateException;
import pl.mwasyluk.ouroom_server.repos.ChatRepository;
import pl.mwasyluk.ouroom_server.services.MemberValidator;
import pl.mwasyluk.ouroom_server.websocket.NotificationTemplate;
import pl.mwasyluk.ouroom_server.websocket.Topic;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static pl.mwasyluk.ouroom_server.dto.notification.NotificationView.Action.CHANGED;
import static pl.mwasyluk.ouroom_server.dto.notification.NotificationView.Action.NEW;
import static pl.mwasyluk.ouroom_server.dto.notification.NotificationView.Action.REMOVED;
import static pl.mwasyluk.ouroom_server.services.PrincipalValidator.validatePrincipal;

@RequiredArgsConstructor

@Service
public class DefaultChatService implements ChatService {
    private final ChatRepository chatRepo;
    private final MemberValidator memberValidator;
    private final NotificationTemplate notificationTemplate;

    private void notifyAllMembers(NotificationView.Action action, ChatPresentableView chatView) {
        NotificationView notificationView = new NotificationView(action, chatView);
        notificationTemplate.notifyAllMembers(chatView.id(), Topic.MEMBERSHIPS, notificationView);
    }

    private void setChatPresentable(Chat chat, String chatName, boolean isClearImage, MultipartFile file) {
        chat.setName(chatName);

        if (isClearImage) {
            chat.setImage(null);
        } else if (file != null) {
            try {
                Media media = Media.of(DataSource.of(file.getBytes()));
                chat.setImage((Image) media);
            } catch (Exception e) {
                throw new ServiceException(UNPROCESSABLE_ENTITY, "The given file is not a valid image.");
            }
        }
    }

    public @NonNull Collection<ChatPresentableView> readAllWithPrincipal() {
        // validation
        User principal = validatePrincipal();

        // execution
        return chatRepo.findAllByUserId(principal.getId()).stream()
                .map(ChatPresentableView::new)
                .collect(Collectors.toList());
    }

    @Override
    public @NonNull ChatPresentableView create(@NonNull ChatForm chatForm) {
        // validation
        User principal = validatePrincipal();

        // execution
        Chat newChat = new Chat(principal);
        setChatPresentable(newChat, chatForm.getName(), false, chatForm.getFile());
        ChatPresentableView chatPresentableView = new ChatPresentableView(chatRepo.save(newChat));

        notifyAllMembers(NEW, chatPresentableView);
        return chatPresentableView;
    }

    @Override
    public @NonNull ChatDetailsView read(@NonNull UUID chatId) {
        // validation
        User principal = validatePrincipal();
        memberValidator.validateAsMember(principal.getId(), chatId);

        // verification
        Optional<ChatDetailsView> optionalView = chatRepo.findDetailsById(chatId);
        if (optionalView.isEmpty()) {
            throw new UnexpectedStateException("Chat could not be found, but the principle is recognized as a member.");
        }

        // execution
        return optionalView.get();
    }

    @Override
    public @NonNull ChatPresentableView update(@NonNull ChatForm chatForm) {
        // validation
        UUID principalId = validatePrincipal().getId();
        if (chatForm.getChatId() == null) {
            throw new ServiceException(UNPROCESSABLE_ENTITY, "Updating Chat requires a chat ID.");
        }
        Member member = memberValidator.validateAsMember(principalId, chatForm.getChatId());

        // verification
        boolean canManageDetails = member.getPrivileges().contains(MemberPrivilege.MANAGE_DETAILS);
        if (!canManageDetails) {
            throw new ServiceException(FORBIDDEN, "Updating Chat requires higher privileges.");
        }

        Optional<Chat> targetChatOptional = chatRepo.findById(chatForm.getChatId());
        if (targetChatOptional.isEmpty()) {
            throw new UnexpectedStateException("Chat could not be found, but the principle is recognized as a member.");
        }

        // execution
        Chat targetChat = targetChatOptional.get();
        setChatPresentable(targetChat, chatForm.getName(), chatForm.isClearImage(), chatForm.getFile());
        ChatPresentableView chatPresentableView = new ChatPresentableView(chatRepo.save(targetChat));

        notifyAllMembers(CHANGED, chatPresentableView);
        return chatPresentableView;
    }

    @Override
    public void delete(@NonNull UUID chatId) {
        // validation
        User principal = validatePrincipal();

        // verification
        Optional<Chat> optionalChat = chatRepo.findById(chatId);
        if (optionalChat.isEmpty()) {
            throw new ServiceException(NOT_FOUND, "Chat with the given ID does not exist.");
        }

        boolean ownerPrincipal = optionalChat.get().getOwner().equals(principal);
        if (!ownerPrincipal) {
            throw new ServiceException(FORBIDDEN, "Deleting Chat requires higher privileges.");
        }

        // execution
        notifyAllMembers(REMOVED, new ChatPresentableView(Chat.mockOf(chatId)));
        chatRepo.deleteById(chatId);
    }
}
