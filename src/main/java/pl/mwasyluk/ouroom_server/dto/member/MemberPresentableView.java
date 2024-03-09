package pl.mwasyluk.ouroom_server.dto.member;

import java.util.Collection;

import pl.mwasyluk.ouroom_server.domain.member.Member;
import pl.mwasyluk.ouroom_server.domain.member.MemberPrivilege;
import pl.mwasyluk.ouroom_server.dto.user.UserPresentableView;

public record MemberPresentableView(
        UserPresentableView user,
        boolean locked,
        Collection<MemberPrivilege> privileges
) {

    public MemberPresentableView(Member member) {
        this(new UserPresentableView(member.getUser()),
                member.isLocked(),
                member.getPrivileges());
    }
}
