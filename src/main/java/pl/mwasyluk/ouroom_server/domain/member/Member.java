package pl.mwasyluk.ouroom_server.domain.member;

import java.util.Set;

import lombok.NonNull;

import pl.mwasyluk.ouroom_server.domain.container.Membership;
import pl.mwasyluk.ouroom_server.domain.user.User;

public interface Member {
    @NonNull User getUser();
    @NonNull Membership getMembership();
    boolean destroy();

    boolean isLocked();
    void setLocked(boolean locked);
    
    @NonNull Set<MemberPrivilege> getPrivileges();
    boolean setPrivileges(Set<MemberPrivilege> privileges);
    boolean hasPrivileges(@NonNull Set<MemberPrivilege> privileges);
}
