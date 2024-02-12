package pl.mwasyluk.ouroom_server.domain.member;

import java.util.Set;

import lombok.NonNull;

import pl.mwasyluk.ouroom_server.domain.user.User;

public class ChatMemberFactory {
    public Member create(@NonNull User user, Set<MemberPrivilege> privileges) {
        return new ChatMember(user, privileges);
    }
}
