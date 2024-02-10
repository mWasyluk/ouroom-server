package pl.mwasyluk.ouroom_server.newdomain.member;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import jakarta.persistence.*;

import pl.mwasyluk.ouroom_server.newdomain.Identifiable;
import pl.mwasyluk.ouroom_server.newdomain.converter.MemberPrivilegeSetConverter;
import pl.mwasyluk.ouroom_server.newdomain.user.User;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "members", indexes = {
        @Index(name = "users_index", columnList = "user_id"),
        @Index(name = "memberships_index", columnList = "membership_id")},
       uniqueConstraints = @UniqueConstraint(name = "uniqueUserAndMembership",
                                             columnNames = {"user_id", "membership_id"}))
public abstract class BaseMember extends Identifiable implements Member {

    @NonNull
    @Setter(AccessLevel.PROTECTED)
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", updatable = false)
    protected User user;

    @NonNull
    @Column(length = 32)
    @Convert(converter = MemberPrivilegeSetConverter.class)
    protected EnumSet<MemberPrivilege> privileges;

    protected BaseMember(@NonNull User user, @NonNull Set<MemberPrivilege> privileges) {
        this.user = user;
        this.privileges = EnumSet.copyOf(privileges);
    }

    @NonNull
    @Override
    public Set<MemberPrivilege> getPrivileges() {
        return Collections.unmodifiableSet(privileges);
    }

    protected void setPrivileges(Set<MemberPrivilege> privileges) {
        this.privileges = EnumSet.copyOf(privileges);
    }

    @Override
    public boolean hasPrivileges(@NonNull Set<MemberPrivilege> privileges) {
        return this.privileges.containsAll(privileges);
    }
}
