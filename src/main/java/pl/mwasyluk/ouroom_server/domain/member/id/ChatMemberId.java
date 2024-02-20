package pl.mwasyluk.ouroom_server.domain.member.id;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import pl.mwasyluk.ouroom_server.domain.container.Chat;
import pl.mwasyluk.ouroom_server.domain.container.Membership;
import pl.mwasyluk.ouroom_server.domain.user.User;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Embeddable
public class ChatMemberId extends MemberId {

    @ManyToOne(targetEntity = Chat.class, fetch = FetchType.LAZY)
    private Membership membership;

    public ChatMemberId(@NonNull User user, @NonNull Chat chat) {
        super(user, chat);
    }

    @Override
    protected void setMembership(@NonNull Membership membership) {
        this.membership = membership;
    }

    @Override
    public void destroy() {
        this.membership = null;
    }
}
