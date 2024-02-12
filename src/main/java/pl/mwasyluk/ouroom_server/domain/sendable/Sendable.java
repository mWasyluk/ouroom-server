package pl.mwasyluk.ouroom_server.domain.sendable;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.NonNull;

import pl.mwasyluk.ouroom_server.domain.container.SendablesContainer;
import pl.mwasyluk.ouroom_server.domain.media.Media;
import pl.mwasyluk.ouroom_server.domain.user.User;

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
    boolean updateMessage(String newMessage);
    boolean updateMedia(Media newMedia);

    SendablesContainer getContainer();
    boolean setContainer(SendablesContainer sendablesContainer);
}
