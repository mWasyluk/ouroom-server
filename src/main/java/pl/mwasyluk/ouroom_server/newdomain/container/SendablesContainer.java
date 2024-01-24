package pl.mwasyluk.ouroom_server.newdomain.container;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lombok.NonNull;

import pl.mwasyluk.ouroom_server.newdomain.sendable.Sendable;

public interface SendablesContainer {
    @NonNull UUID getId();

    @NonNull Set<Sendable> getAllSendables();
    @NonNull Optional<Sendable> getSendableById(@NonNull UUID sendableId);
    
    boolean putSendable(@NonNull Sendable sendable);

    boolean removeSendable(@NonNull Sendable sendable);
    boolean removeSendableById(@NonNull UUID sendableId);
}
