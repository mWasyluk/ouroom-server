package pl.mwasyluk.ouroom_server.dto.user;

import java.util.UUID;

import pl.mwasyluk.ouroom_server.domain.user.User;

public record UserPresentableView(
        UUID id,
        String name,
        UUID imageId,
        String imageUrl
) {

    public UserPresentableView(User user) {
        this(user.getId(),
                user.getName(),
                user.getImage() == null ? null : user.getImage().getId(),
                user.getImageUrl());
    }
}
