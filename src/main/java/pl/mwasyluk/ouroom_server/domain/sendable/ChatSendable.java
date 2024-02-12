package pl.mwasyluk.ouroom_server.domain.sendable;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import pl.mwasyluk.ouroom_server.domain.container.Chat;
import pl.mwasyluk.ouroom_server.domain.container.SendablesContainer;
import pl.mwasyluk.ouroom_server.domain.media.Media;
import pl.mwasyluk.ouroom_server.domain.user.User;

/**
 Chat sendable is a flexible entity that can contain a text message and a reference to media entity.
 <br> The text message is nullable but never blank String object (trimmed during instantiation or update).
 <br> The media entity is optional for not blank text messages, otherwise mandatory.
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
@Table(indexes = {@Index(name = "containers_index", columnList = "container_id")})
public class ChatSendable extends BaseSendable {

    @ManyToOne(targetEntity = Chat.class)
    protected SendablesContainer container;

    protected ChatSendable(@NonNull User creator, String message, Media media) {
        super(creator, message, media);
    }

    protected String validateMessage(String message) {
        return message == null || message.isBlank() ? null : message.trim();
    }

    protected Media validateMedia(Media media) {
        return media == null || media.getContentSize() <= 0 ? null : media;
    }

    @Override
    public boolean updateState(SendableState newState) {
        if (newState != null && state.ordinal() <= newState.ordinal()) {
            state = newState;
            return true;
        }
        return false;
    }

    @Override
    public boolean updateMessage(String newMessage) {
        if (!isValid(newMessage, this.media)) {
            return false;
        }
        String validatedMessage = validateMessage(newMessage);

        if (this.message != null && !this.message.equals(validatedMessage)
            || this.message == null && validatedMessage != null) {
            this.message = validatedMessage;
            this.edited = true;
        }
        return true;
    }

    @Override
    public boolean updateMedia(Media newMedia) {
        if (!isValid(this.message, newMedia)) {
            return false;
        }
        Media validatedMedia = validateMedia(newMedia);

        if (this.media != null && !this.media.equals(validatedMedia)
            || this.media == null && validatedMedia != null) {
            this.media = validatedMedia;
            this.edited = true;
        }
        return true;
    }

    @Override
    public SendablesContainer getContainer() {
        return container;
    }

    @Override
    public boolean setContainer(@NonNull SendablesContainer container) {
        this.container = container;
        return true;
    }

    @Override
    protected void setMedia(Media media) {
        this.media = validateMedia(media);
    }

    @Override
    protected void setMessage(String message) {
        this.message = validateMessage(message);
    }

    @Override
    protected boolean isValid(String message, Media media) {
        return validateMessage(message) != null || validateMedia(media) != null;
    }
}
