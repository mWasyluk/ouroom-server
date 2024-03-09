package pl.mwasyluk.ouroom_server.services;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import pl.mwasyluk.ouroom_server.domain.container.Chat;
import pl.mwasyluk.ouroom_server.domain.member.ChatMember;
import pl.mwasyluk.ouroom_server.domain.member.Member;
import pl.mwasyluk.ouroom_server.domain.member.MemberPrivilege;
import pl.mwasyluk.ouroom_server.domain.member.id.ChatMemberId;
import pl.mwasyluk.ouroom_server.domain.user.User;
import pl.mwasyluk.ouroom_server.exceptions.ServiceException;
import pl.mwasyluk.ouroom_server.repos.MemberRepository;

@RequiredArgsConstructor

@Component
public class MemberValidator {
    private final MemberRepository memberRepo;

    public @NonNull Member validateAsMember(@NonNull UUID userId, @NonNull UUID chatId) {
        Optional<ChatMember> optionalMember
                = memberRepo.findById(new ChatMemberId(User.mockOf(userId), Chat.mockOf(chatId)));

        if (optionalMember.isEmpty()) {
            throw new ServiceException(HttpStatus.FORBIDDEN, "This operation requires member privileges.");
        }

        return optionalMember.get();
    }

    public @NonNull Member validatePrivilegesAsMember(@NonNull UUID userId, @NonNull UUID chatId,
            EnumSet<MemberPrivilege> privileges) {
        Member member = validateAsMember(userId, chatId);

        if (!member.getPrivileges().containsAll(privileges)) {
            throw new ServiceException(HttpStatus.FORBIDDEN, "This operation requires higher member privileges.");
        }

        return member;
    }

    public @NonNull Member validatePrivilegesAsMember(@NonNull UUID userId, @NonNull UUID chatId,
            MemberPrivilege privilege) {
        return validatePrivilegesAsMember(userId, chatId, EnumSet.of(privilege));
    }
}
