package pl.mwasyluk.ouroom_server.domain.member.id;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import pl.mwasyluk.ouroom_server.domain.container.Membership;
import pl.mwasyluk.ouroom_server.domain.user.User;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@MappedSuperclass
public abstract class MemberId {
    @Setter(AccessLevel.PRIVATE)
    @ManyToOne(optional = false)
    @JoinColumn(updatable = false, nullable = false)
    protected User user;

    public MemberId(@NonNull User user, @NonNull Membership membership) {
        this.user = user;
        this.setMembership(membership);
    }

    abstract protected void setMembership(@NonNull Membership membership);
    abstract public void destroy();
}
