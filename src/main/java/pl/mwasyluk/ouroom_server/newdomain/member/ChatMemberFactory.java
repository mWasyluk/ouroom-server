package pl.mwasyluk.ouroom_server.newdomain.member;

import java.util.Collection;
import java.util.HashSet;

import org.springframework.stereotype.Component;
import lombok.NonNull;

import pl.mwasyluk.ouroom_server.newdomain.container.Chat;
import pl.mwasyluk.ouroom_server.newdomain.user.User;

@Component
public class ChatMemberFactory implements MembersFactory {
    @Override
    public Member createMember(@NonNull User user) {
        return this.createMember(user, Chat.MEMBER_PRIVILEGES);
    }

    @Override
    public Member createMember(@NonNull User user, @NonNull Collection<MemberPrivilege> privileges) {
        return new ChatMember(user, new HashSet<>(privileges));
    }

    @Override
    public Member createAdmin(@NonNull User user) {
        return new ChatMember(user, Chat.ADMIN_PRIVILEGES);
    }
}
