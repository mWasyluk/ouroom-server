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
    @NonNull SendableState getState();
    @NonNull SendableType getType();
    String getMessage();
    Media getMedia();
    boolean isEdited();

    boolean updateState(SendableState newState);
    boolean updateContent(String message, Media media);

    SendablesContainer getContainer();
    boolean setContainer(SendablesContainer sendablesContainer);
}
