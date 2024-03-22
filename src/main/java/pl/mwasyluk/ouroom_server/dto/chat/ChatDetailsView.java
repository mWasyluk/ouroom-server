package pl.mwasyluk.ouroom_server.dto.chat;

import java.util.UUID;

import pl.mwasyluk.ouroom_server.domain.container.Chat;

public record ChatDetailsView(
        UUID id,
        String name,
        UUID imageId,
        int membersAmount,
        int sendablesAmount
) {
    public ChatDetailsView(Chat chat) {
        this(
                chat.getId(),
                chat.getName(),
                chat.getImage() == null ? null : chat.getImage().getId(),
                chat.getAllMembers().size(),
                chat.getAllSendables().size()
        );
    }
}
