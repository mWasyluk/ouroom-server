package pl.mwasyluk.ouroom_server.newdomain.sendable;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.NonNull;

import pl.mwasyluk.ouroom_server.newdomain.container.SendablesContainer;
import pl.mwasyluk.ouroom_server.newdomain.media.Media;
import pl.mwasyluk.ouroom_server.newdomain.user.User;

public interface Sendable {
    @NonNull UUID getId();

    @NonNull User getCreator();
    @NonNull LocalDateTime getCreatedAt();

    /**
     Safely updates all not effectively immutable fields with fields from the given Sendable. In that purpose the method
     takes advantage of {@link #updateState(SendableState)} and {@link Sendable#updateContent(String, Media)}.

     @param sendable
     instance of Sendable with the fields to be copied from
     */
    default void updateMutableFieldsWith(@NonNull Sendable sendable) {
        this.updateState(sendable.getState());
        this.updateContent(sendable.getMessage(), sendable.getMedia());
    }

    /**
     Updates the state field of this instance with the given value.

     @param newState
     the new {@link SendableState SendableState } enum value to be applied
     */
    boolean updateState(SendableState newState);
    @NonNull SendableState getState();
    @NonNull SendableType getType();

    /**
     Updates the message and media fields of this instance with the given objects.

     @param message
     {@link String} value with the message to be applied
     @param media
     {@link Media} instance to be applied
     */
    boolean updateContent(String message, Media media);
    String getMessage();
    Media getMedia();
    boolean isEdited();

    SendablesContainer getContainer();
    /**
     Relationship between Sendable and SendablesContainer happens on the Chat side. Therefore, this method is not
     recommended for changing an assigned relationship outside of entities.

     @param sendablesContainer
     collection of Sendables responsible for relationship creation

     @see SendablesContainer
     */
    boolean setContainer(SendablesContainer sendablesContainer);
}
