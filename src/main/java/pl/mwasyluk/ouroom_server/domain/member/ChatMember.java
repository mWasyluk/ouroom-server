package pl.mwasyluk.ouroom_server.domain.member;

import java.util.EnumSet;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

import pl.mwasyluk.ouroom_server.converters.MemberPrivilegeSetConverter;
import pl.mwasyluk.ouroom_server.domain.container.Chat;
import pl.mwasyluk.ouroom_server.domain.container.Membership;
import pl.mwasyluk.ouroom_server.domain.member.id.ChatMemberId;
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
public class ChatMember implements Member {
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    @EmbeddedId
    private ChatMemberId memberId;

    @NonNull
    @Column(length = 32)
    @Convert(converter = MemberPrivilegeSetConverter.class)
    protected EnumSet<MemberPrivilege> privileges;

    private boolean locked;

    public ChatMember(@NonNull User user, @NonNull Membership membership, Set<MemberPrivilege> privileges) {
        this.memberId = new ChatMemberId(user, (Chat) membership);
        setPrivileges(privileges);
    }

    @Override
    public @NonNull User getUser() {
        return memberId.getUser();
    }

    @Override
    public @NonNull Membership getMembership() {
        return memberId.getMembership();
    }

    @Override
    public boolean destroy() {
        if (locked) {
            return false;
        }
        memberId.destroy();
        return true;
    }

    @Override
    public boolean setPrivileges(Set<MemberPrivilege> privileges) {
        if (locked) {
            return false;
        }
        this.privileges = privileges == null || privileges.isEmpty()
                ? EnumSet.noneOf(MemberPrivilege.class)
                : EnumSet.copyOf(privileges);
        return true;
    }

    @Override
    public boolean hasPrivileges(@NonNull Set<MemberPrivilege> privileges) {
        return this.privileges.containsAll(privileges);
    }
}
