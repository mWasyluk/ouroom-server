package pl.mwasyluk.ouroom_server.domain.sendable;

import java.time.ZonedDateTime;
import java.util.UUID;

import lombok.NonNull;

import pl.mwasyluk.ouroom_server.domain.container.SendablesContainer;
import pl.mwasyluk.ouroom_server.domain.user.User;

public interface Sendable {
    @NonNull UUID getId();
    @NonNull User getCreator();
    @NonNull ZonedDateTime getCreatedAt();
    @NonNull SendableState getState();
    @NonNull String getMessage();
    boolean isEdited();

    boolean updateState(SendableState newState);
    boolean updateMessage(String newMessage);

    SendablesContainer getContainer();
    boolean setContainer(SendablesContainer sendablesContainer);
}
