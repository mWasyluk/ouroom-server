package pl.mwasyluk.ouroom_server.newdomain.member;

import java.util.Collection;

import lombok.NonNull;

import pl.mwasyluk.ouroom_server.newdomain.user.User;

public interface MembersFactory {
    Member createMember(@NonNull User user);
    Member createMember(@NonNull User user, @NonNull Collection<MemberPrivilege> privileges);
    Member createAdmin(@NonNull User user);
}
