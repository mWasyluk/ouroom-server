package pl.mwasyluk.ouroom_server.domain.sendable;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;

import pl.mwasyluk.ouroom_server.domain.Identifiable;
import pl.mwasyluk.ouroom_server.domain.media.BaseMedia;
import pl.mwasyluk.ouroom_server.domain.media.Media;
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
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    protected SendableType type;

    @NonNull
    @Setter(AccessLevel.PROTECTED)
    protected LocalDateTime createdAt;

    @NonNull
    @Setter(AccessLevel.PROTECTED)
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    protected SendableState state;

    @Setter(AccessLevel.PROTECTED)
    protected String message;

    @Setter(AccessLevel.PROTECTED)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = BaseMedia.class)
    protected Media media;

    @Setter(AccessLevel.PROTECTED)
    protected boolean edited;

    protected BaseSendable(@NonNull User creator, String message, Media media) {
        if (!isValid(message, media)) {
            throw new IllegalArgumentException("Cannot instantiate Sendable without a valid content.");
        }
        setMessage(message);
        setMedia(media);
        updateTypeBasedOnContent();

        this.creator = creator;
        this.createdAt = LocalDateTime.now();
        this.state = SendableState.CREATED;
    }

    abstract protected boolean isValid(String message, Media media);

    protected void updateTypeBasedOnContent() {
        this.type = media != null ? SendableType.MEDIA : SendableType.TEXT;
    }
}
