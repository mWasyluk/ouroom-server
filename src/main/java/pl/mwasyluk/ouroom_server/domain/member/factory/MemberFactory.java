package pl.mwasyluk.ouroom_server.domain.member.factory;

import java.util.Set;

import lombok.NonNull;
import lombok.Setter;

import pl.mwasyluk.ouroom_server.domain.container.Membership;
import pl.mwasyluk.ouroom_server.domain.member.Member;
import pl.mwasyluk.ouroom_server.domain.member.MemberPrivilege;
import pl.mwasyluk.ouroom_server.domain.user.User;

public abstract class MemberFactory {
    @Setter
    protected Membership membership;

    public MemberFactory(Membership membership) {
        this.membership = membership;
    }

    public Member create(@NonNull User user, Set<MemberPrivilege> privileges) {
        return generateMember(user, privileges);
    }

    abstract protected Member generateMember(@NonNull User user, Set<MemberPrivilege> privileges);
}
