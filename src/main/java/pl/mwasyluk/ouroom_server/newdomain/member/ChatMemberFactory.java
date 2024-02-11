package pl.mwasyluk.ouroom_server.newdomain.member;

import java.util.Set;

import lombok.NonNull;

import pl.mwasyluk.ouroom_server.newdomain.user.User;

public class ChatMemberFactory {
    public Member create(@NonNull User user, Set<MemberPrivilege> privileges) {
        return new ChatMember(user, privileges);
    }
}
