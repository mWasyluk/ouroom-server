package pl.mwasyluk.ouroom_server.dto.chat;

import java.util.UUID;

import pl.mwasyluk.ouroom_server.domain.container.Chat;

public record ChatPresentableView(
        UUID id,
        String name,
        UUID imageId,
        String imageUrl
) {
    public ChatPresentableView(Chat chat) {
        this(chat.getId(),
                chat.getName(),
                chat.getImage() == null ? null : chat.getImage().getId(),
                chat.getImageUrl());
    }
}
