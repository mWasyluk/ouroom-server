package pl.mwasyluk.ouroom_server.services.sendable;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import pl.mwasyluk.ouroom_server.domain.container.Chat;
import pl.mwasyluk.ouroom_server.domain.member.MemberPrivilege;
import pl.mwasyluk.ouroom_server.domain.sendable.ChatSendable;
import pl.mwasyluk.ouroom_server.domain.user.User;
import pl.mwasyluk.ouroom_server.dto.sendable.SendableForm;
import pl.mwasyluk.ouroom_server.dto.sendable.SendableView;
import pl.mwasyluk.ouroom_server.exceptions.ServiceException;
import pl.mwasyluk.ouroom_server.repos.SendableRepository;
import pl.mwasyluk.ouroom_server.services.MemberValidator;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static pl.mwasyluk.ouroom_server.services.PrincipalValidator.validatePrincipal;

@RequiredArgsConstructor

@Service
public class DefaultSendableService implements SendableService {
    private final SendableRepository sendableRepo;
    private final MemberValidator memberValidator;

    @Override
    public @NonNull Collection<SendableView> readAllFromContainer(@NonNull UUID containerId) {
        // verification
        memberValidator.validateAsMember(validatePrincipal().getId(), containerId);

        // execution
        return sendableRepo.findAllByContainerId(containerId).stream()
                .map(SendableView::new)
                .collect(Collectors.toList());
    }

    @Override
    public @NonNull SendableView create(@NonNull SendableForm sendableForm) {
        // validation
        User principal = validatePrincipal();
        if (sendableForm.getContainerId() == null || sendableForm.getMessage() == null) {
            throw new ServiceException(UNPROCESSABLE_ENTITY,
                    "Sendable requires a target container ID and a non-empty String value.");
        }

        // verification
        memberValidator.validatePrivilegesAsMember(principal.getId(), sendableForm.getContainerId(),
                MemberPrivilege.ADD_MESSAGES);

        // execution
        ChatSendable targetSendable = new ChatSendable(principal, sendableForm.getMessage());
        targetSendable.setContainer(Chat.mockOf(sendableForm.getContainerId()));
        return new SendableView(sendableRepo.save(targetSendable));
    }

    @Override
    public @NonNull SendableView update(@NonNull SendableForm sendableForm) {
        // validation
        User principal = validatePrincipal();
        if (sendableForm.getSendableId() == null) {
            throw new ServiceException(UNPROCESSABLE_ENTITY, "Updating Sendable requires its ID.");
        }

        // verification
        Optional<ChatSendable> optionalSendable = sendableRepo.findById(sendableForm.getSendableId());
        if (optionalSendable.isEmpty()) {
            throw new ServiceException(NOT_FOUND, "Sendable with the given ID does not exist.");
        }
        ChatSendable targetSendable = optionalSendable.get();
        if (!targetSendable.getCreator().equals(principal)) {
            throw new ServiceException(FORBIDDEN, "Only creators are allowed to modify Sendables.");
        }

        // execution
        boolean failed = !targetSendable.updateMessage(sendableForm.getMessage());
        if (failed) {
            throw new ServiceException(UNPROCESSABLE_ENTITY, "The new content cannot be applied to this Sendable.");
        }

        return new SendableView(sendableRepo.save(targetSendable));
    }

    @Override
    public void delete(@NonNull UUID sendableId) {
        // validation
        User principal = validatePrincipal();
        Optional<ChatSendable> optionalSendable = sendableRepo.findById(sendableId);
        if (optionalSendable.isEmpty()) {
            throw new ServiceException(NOT_FOUND, "Sendable with the given ID does not exist.");
        }

        // verification + execution
        ChatSendable targetSendable = optionalSendable.get();
        if (targetSendable.getCreator().equals(principal)) {
            sendableRepo.deleteById(sendableId);
            return;
        }

        // verification + execution
        memberValidator.validatePrivilegesAsMember(principal.getId(), targetSendable.getContainer().getId(),
                MemberPrivilege.DELETE_MESSAGES);
        sendableRepo.deleteById(sendableId);
    }
}
