package pl.mwasyluk.ouroom_server.newdomain.sendable;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import pl.mwasyluk.ouroom_server.newdomain.container.Chat;
import pl.mwasyluk.ouroom_server.newdomain.container.SendablesContainer;
import pl.mwasyluk.ouroom_server.newdomain.media.Media;
import pl.mwasyluk.ouroom_server.newdomain.user.User;

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

    @Override
    protected boolean isValid(String message, Media media) {
        if (media == null || media.getContentSize() <= 0) {
            return message != null && !message.isBlank();
        }
        return true;
    }

    /**
     {@inheritDoc}<br> <b>The method is safe to use with any value</b, but does not allow to reduce the state.>

     @param newState
     the new {@link SendableState SendableState } enum value to be applied

     @return true - if the
     */
    @Override
    public boolean updateState(SendableState newState) {
        if (newState != null && state.ordinal() <= newState.ordinal()) {
            state = newState;
            return true;
        }
        return false;
    }

    /**
     {@inheritDoc}<br> <b>The method is safe to use with any values</b>, even those that are equal to the current ones.
     In this case, this method ensures none of the fields of this instance will be overwritten, including
     {@link #edited}.

     @param message
     {@link String} value with the message to be applied
     @param media
     {@link Media} instance to be applied

     @return false - if the given values are not valid due to {@link #isValid}
     <br>    true - otherwise (does not guarantee that the fields of this instance will change at all)
     */
    @Override
    public boolean updateContent(String message, Media media) {
        if (!isValid(message, media)) {
            return false;
        }

        boolean isUpdated = false;
        if (message != null && !message.isBlank() && !message.trim().equals(this.message)) {
            this.message = message.trim();
            isUpdated = true;
        } else if (message == null && this.message != null) {
            this.message = null;
            isUpdated = true;
        }

        if ((this.media != null && media == null)
            || (media != null && !media.equals(this.media))) {
            this.media = media;
            isUpdated = true;
        }

        if (isUpdated) {
            this.updateTypeBasedOnContent();
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
}
