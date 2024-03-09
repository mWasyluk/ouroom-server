package pl.mwasyluk.ouroom_server.dto.sendable;

import java.util.UUID;

import lombok.Data;

@Data
public class SendableForm {
    private UUID sendableId;
    private UUID containerId;
    private String message;

    public String getMessage() {
        return message == null || message.isBlank() ? null : message.trim();
    }
}
