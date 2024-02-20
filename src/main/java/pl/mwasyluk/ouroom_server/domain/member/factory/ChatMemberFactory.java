package pl.mwasyluk.ouroom_server.domain.member.factory;

import java.util.Set;

import lombok.NonNull;

import pl.mwasyluk.ouroom_server.domain.container.Chat;
import pl.mwasyluk.ouroom_server.domain.member.ChatMember;
import pl.mwasyluk.ouroom_server.domain.member.MemberPrivilege;
import pl.mwasyluk.ouroom_server.domain.user.User;

public class ChatMemberFactory extends MemberFactory {
    public ChatMemberFactory(Chat chat) {
        super(chat);
    }

    @Override
    public ChatMember create(@NonNull User user, Set<MemberPrivilege> privileges) {
        return (ChatMember) super.create(user, privileges);
    }

    @Override
    protected ChatMember generateMember(@NonNull User user, Set<MemberPrivilege> privileges) {
        return new ChatMember(user, membership, privileges);
    }
}
