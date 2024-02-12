package pl.mwasyluk.ouroom_server.domain.container;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import lombok.NonNull;

import pl.mwasyluk.ouroom_server.domain.sendable.Sendable;

public interface SendablesContainer {
    @NonNull UUID getId();

    @NonNull Collection<Sendable> getAllSendables();
    @NonNull Optional<Sendable> getSendableById(@NonNull UUID sendableId);
    boolean addSendable(@NonNull Sendable sendable);
    boolean removeSendableById(@NonNull UUID sendableId);
}
