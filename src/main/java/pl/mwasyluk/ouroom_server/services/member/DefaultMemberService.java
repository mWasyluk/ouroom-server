package pl.mwasyluk.ouroom_server.services.member;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import pl.mwasyluk.ouroom_server.domain.container.Chat;
import pl.mwasyluk.ouroom_server.domain.member.ChatMember;
import pl.mwasyluk.ouroom_server.domain.member.MemberPrivilege;
import pl.mwasyluk.ouroom_server.domain.member.factory.ChatMemberFactory;
import pl.mwasyluk.ouroom_server.domain.user.User;
import pl.mwasyluk.ouroom_server.dto.chat.ChatPresentableView;
import pl.mwasyluk.ouroom_server.dto.member.MemberPresentableView;
import pl.mwasyluk.ouroom_server.dto.member.MembersForm;
import pl.mwasyluk.ouroom_server.dto.notification.NotificationView;
import pl.mwasyluk.ouroom_server.exceptions.ServiceException;
import pl.mwasyluk.ouroom_server.exceptions.UnexpectedStateException;
import pl.mwasyluk.ouroom_server.repos.ChatRepository;
import pl.mwasyluk.ouroom_server.repos.MemberRepository;
import pl.mwasyluk.ouroom_server.repos.UserRepository;
import pl.mwasyluk.ouroom_server.services.MemberValidator;
import pl.mwasyluk.ouroom_server.websocket.NotificationTemplate;
import pl.mwasyluk.ouroom_server.websocket.Topic;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static pl.mwasyluk.ouroom_server.dto.notification.NotificationView.Action.NEW;
import static pl.mwasyluk.ouroom_server.dto.notification.NotificationView.Action.REMOVED;
import static pl.mwasyluk.ouroom_server.services.PrincipalValidator.validatePrincipal;

@RequiredArgsConstructor

@Service
public class DefaultMemberService implements MemberService {
    private final UserRepository userRepo;
    private final MemberRepository memberRepository;
    private final ChatRepository chatRepository;
    private final MemberValidator memberValidator;
    private final NotificationTemplate notificationTemplate;

    private void notifyAllUsers(Set<UUID> userIdSet, NotificationView.Action action, ChatPresentableView chatView) {
        NotificationView notificationView = new NotificationView(action, chatView);
        notificationTemplate.notifyAllUsers(userIdSet, Topic.MEMBERSHIPS, notificationView);
    }

    @Override
    public @NonNull Collection<MemberPresentableView> readAllInMembership(@NonNull UUID membershipId) {
        // validation
        memberValidator.validateAsMember(validatePrincipal().getId(), membershipId);

        // execution
        return memberRepository.findAllByMembershipId(membershipId).stream()
                .map(MemberPresentableView::new)
                .collect(Collectors.toList());
    }

    public @NonNull Collection<MemberPresentableView> createAll(@NonNull MembersForm membersForm) {
        // validation
        User principal = validatePrincipal();
        if (membersForm.getMembershipId() == null
            || membersForm.getMembers() == null || membersForm.getMembers().size() == 0) {
            throw new ServiceException(UNPROCESSABLE_ENTITY, "Creating new members requires a membership ID "
                                                             + "and a non-empty map of members' ID and privileges.");
        }
        memberValidator.validatePrivilegesAsMember(principal.getId(), membersForm.getMembershipId(),
                MemberPrivilege.MANAGE_MEMBERS);

        // verification
        Set<UUID> requestedUserIds = membersForm.getMembers().keySet();
        boolean someNonExistentUser = !userRepo.allExistByIdIn(requestedUserIds);
        if (someNonExistentUser) {
            throw new ServiceException(NOT_FOUND, "User with the given ID does not exist.");
        }
        boolean someAlreadyMember = memberRepository.anyExists(requestedUserIds, membersForm.getMembershipId());
        if (someAlreadyMember) {
            throw new ServiceException(CONFLICT, "User with the given ID is already a member.");
        }
        Optional<Chat> optionalChat = chatRepository.findById(membersForm.getMembershipId());
        if (optionalChat.isEmpty()) {
            throw new UnexpectedStateException("Chat could not be found, but members have been already validated.");
        }
        Chat targetChat = optionalChat.get();

        // execution
        ChatMemberFactory memberFactory = new ChatMemberFactory(targetChat);
        Set<ChatMember> members = membersForm.getMembers().entrySet().stream()
                .map(e -> memberFactory.create(User.mockOf(e.getKey()), e.getValue()))
                .collect(Collectors.toSet());

        notifyAllUsers(requestedUserIds, NEW, new ChatPresentableView(targetChat));
        return memberRepository.saveAll(members).stream()
                .map(MemberPresentableView::new)
                .collect(Collectors.toList());
    }

    @Override
    public @NonNull Collection<MemberPresentableView> updateAll(@NonNull MembersForm membersForm) {
        // validation
        User principal = validatePrincipal();
        if (membersForm.getMembershipId() == null
            || membersForm.getMembers() == null || membersForm.getMembers().size() == 0) {
            throw new ServiceException(UNPROCESSABLE_ENTITY, "Updating members requires a membership ID "
                                                             + "and a non-empty map of members' ID and privileges.");
        }
        memberValidator.validatePrivilegesAsMember(principal.getId(), membersForm.getMembershipId(),
                MemberPrivilege.MANAGE_MEMBERS);

        // verification
        List<ChatMember> members = memberRepository
                .findAllByUserIdIn(membersForm.getMembers().keySet(), membersForm.getMembershipId());
        if (members.size() < membersForm.getMembers().size()) {
            throw new ServiceException(NOT_FOUND, "Member with the given ID does not exist.");
        }

        // execution
        boolean someFailed = members.stream()
                .anyMatch(m -> !m.setPrivileges(membersForm.getMembers().get(m.getUser().getId())));
        if (someFailed) {
            throw new ServiceException(CONFLICT, "Some members could not be updated. Verify if they are locked.");
        }

        return memberRepository.saveAll(members).stream()
                .map(MemberPresentableView::new)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(@NonNull MembersForm membersForm) {
        // validation
        User principal = validatePrincipal();
        if (membersForm.getMembershipId() == null
            || membersForm.getMembers() == null || membersForm.getMembers().size() == 0) {
            throw new ServiceException(UNPROCESSABLE_ENTITY, "Deleting members requires a membership ID "
                                                             + "and a non-empty map of members' ID.");
        }
        memberValidator.validatePrivilegesAsMember(principal.getId(), membersForm.getMembershipId(),
                MemberPrivilege.MANAGE_MEMBERS);

        // verification
        List<ChatMember> members = memberRepository
                .findAllByUserIdIn(membersForm.getMembers().keySet(), membersForm.getMembershipId());
        if (members.size() < membersForm.getMembers().size()) {
            throw new ServiceException(NOT_FOUND, "Member with the given ID does not exist.");
        }
        if (members.stream().anyMatch(ChatMember::isLocked)) {
            throw new ServiceException(CONFLICT, "Some members are locked and cannot be deleted.");
        }

        // execution
        notifyAllUsers(membersForm.getMembers().keySet(), REMOVED,
                new ChatPresentableView(Chat.mockOf(membersForm.getMembershipId())));
        memberRepository.deleteAll(members);
    }
}
