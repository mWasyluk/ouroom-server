package pl.mwasyluk.ouroom_server.domain.member;

import java.util.Set;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import pl.mwasyluk.ouroom_server.domain.container.Chat;
import pl.mwasyluk.ouroom_server.domain.container.Membership;
import pl.mwasyluk.ouroom_server.domain.user.User;

/**
 Chat member is an entity that connects chat with its member users and their privileges.
 <br> Member privilege set can be empty (the user can only read messages) or contain any of available
 {@link MemberPrivilege}. A member with all available privileges is considered a chat admin.
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
public class ChatMember extends BaseMember {
    @ManyToOne(targetEntity = Chat.class)
    private Membership membership;

    protected ChatMember(@NonNull User user, Set<MemberPrivilege> privileges) {
        super(user, privileges);
    }

    @Override
    public boolean setMembership(Membership membership) {
        this.membership = membership;
        return true;
    }
}
