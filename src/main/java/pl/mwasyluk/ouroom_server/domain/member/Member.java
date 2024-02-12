package pl.mwasyluk.ouroom_server.domain.member;

import java.util.Set;
import java.util.UUID;

import lombok.NonNull;

import pl.mwasyluk.ouroom_server.domain.container.Membership;
import pl.mwasyluk.ouroom_server.domain.user.User;

public interface Member {
    @NonNull UUID getId();
    @NonNull User getUser();

    @NonNull Set<MemberPrivilege> getPrivileges();
    boolean setPrivileges(Set<MemberPrivilege> privileges);
    boolean hasPrivileges(@NonNull Set<MemberPrivilege> privileges);

    Membership getMembership();
    boolean setMembership(Membership membership);
}
