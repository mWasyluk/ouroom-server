package pl.mwasyluk.ouroom_server.domain.sendable;

import java.time.ZonedDateTime;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import pl.mwasyluk.ouroom_server.domain.Identifiable;
import pl.mwasyluk.ouroom_server.domain.user.User;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@MappedSuperclass
public abstract class BaseSendable extends Identifiable implements Sendable {
    @NonNull
    @Setter(AccessLevel.PROTECTED)
    @ManyToOne(optional = false)
    protected User creator;

    @NonNull
    @Setter(AccessLevel.PROTECTED)
    protected ZonedDateTime createdAt;

    @NonNull
    @Setter(AccessLevel.PROTECTED)
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    protected SendableState state;

    @NonNull
    @Setter(AccessLevel.PROTECTED)
    @Column(columnDefinition = "text")
    protected String message;

    @Setter(AccessLevel.PROTECTED)
    protected boolean edited;

    protected BaseSendable(@NonNull User creator, @NonNull String message) {
        initMessage(message);
        this.creator = creator;
        this.createdAt = ZonedDateTime.now();
        this.state = SendableState.SENT;
    }

    abstract protected void initMessage(String message);

    @Override
    public boolean updateState(@NonNull SendableState newState) {
        if (newState.ordinal() < this.state.ordinal()) {
            return false;
        }
        this.state = newState;
        return true;
    }

    @Override
    public boolean updateMessage(@NonNull String newMessage) {
        if (newMessage.isBlank()) {
            return false;
        }
        String trimmed = newMessage.trim();
        if (this.message.equals(trimmed)) {
            return true;
        }

        this.message = trimmed;
        this.edited = true;
        return true;
    }
}
