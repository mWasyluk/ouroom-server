package pl.mwasyluk.ouroom_server.dto.chat;

import java.util.UUID;

public record ChatDetailsView(
        UUID id,
        String name,
        UUID imageId,
        int membersAmount,
        int sendablesAmount
) {
}
