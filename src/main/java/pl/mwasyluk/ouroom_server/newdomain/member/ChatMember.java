package pl.mwasyluk.ouroom_server.newdomain.member;

import java.util.Set;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import pl.mwasyluk.ouroom_server.newdomain.container.Chat;
import pl.mwasyluk.ouroom_server.newdomain.container.Membership;
import pl.mwasyluk.ouroom_server.newdomain.user.User;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
public class ChatMember extends BaseMember {
    @ManyToOne(targetEntity = Chat.class)
    private Membership membership;

    protected ChatMember(@NonNull User user, @NonNull Set<MemberPrivilege> privileges) {
        super(user, privileges);
    }

    @Override
    public boolean setMembership(Membership membership) {
        this.membership = membership;
        return true;
    }
}