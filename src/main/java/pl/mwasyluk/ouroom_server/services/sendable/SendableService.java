package pl.mwasyluk.ouroom_server.services.sendable;

import java.util.Collection;
import java.util.UUID;

import lombok.NonNull;

import pl.mwasyluk.ouroom_server.dto.sendable.SendableForm;
import pl.mwasyluk.ouroom_server.dto.sendable.SendableView;

public interface SendableService {
    @NonNull Collection<SendableView> readAllFromContainer(@NonNull UUID containerId);
    @NonNull SendableView create(@NonNull SendableForm sendableForm);
    @NonNull SendableView update(@NonNull SendableForm sendableForm);
    void delete(@NonNull UUID sendableId);
}
