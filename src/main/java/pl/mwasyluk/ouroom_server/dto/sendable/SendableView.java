package pl.mwasyluk.ouroom_server.dto.sendable;

import java.time.ZonedDateTime;
import java.util.UUID;

import pl.mwasyluk.ouroom_server.domain.sendable.Sendable;

public record SendableView(
        UUID id,
        UUID containerId,
        UUID creatorId,
        ZonedDateTime createdAt,
        String state,
        String message,
        boolean edited
) {

    public SendableView(Sendable sendable) {
        this(sendable.getId(),
                sendable.getContainer().getId(),
                sendable.getCreator().getId(),
                sendable.getCreatedAt(),
                sendable.getState().name(),
                sendable.getMessage(),
                sendable.isEdited());
    }
}
