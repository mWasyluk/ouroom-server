package pl.mwasyluk.ouroom_server.newdomain.container;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lombok.NonNull;

import pl.mwasyluk.ouroom_server.newdomain.member.Member;
import pl.mwasyluk.ouroom_server.newdomain.member.MemberPrivilege;
import pl.mwasyluk.ouroom_server.newdomain.user.User;

public interface Membership {
    @NonNull UUID getId();

    @NonNull Collection<Member> getAllMembers();
    Optional<Member> getMemberByUserId(@NonNull UUID userId);
    boolean addMember(@NonNull User user, Set<MemberPrivilege> privileges);
    boolean removeMemberByUserId(@NonNull UUID userId);
}
