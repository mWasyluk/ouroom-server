package pl.mwasyluk.ouroom_server.newdomain.container;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lombok.NonNull;

import pl.mwasyluk.ouroom_server.newdomain.member.Member;

public interface Membership {
    @NonNull UUID getId();

    @NonNull Set<Member> getAllMembers();
    Optional<Member> getMemberById(@NonNull UUID memberId);
    Optional<Member> getMemberByUserId(@NonNull UUID userId);

    boolean putMember(@NonNull Member member);

    boolean removeMemberById(@NonNull UUID memberId);
    boolean removeMemberByUserId(@NonNull UUID userId);
}
