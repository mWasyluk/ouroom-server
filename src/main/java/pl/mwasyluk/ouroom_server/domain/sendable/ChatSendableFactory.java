package pl.mwasyluk.ouroom_server.domain.sendable;

import lombok.NonNull;

import pl.mwasyluk.ouroom_server.domain.media.Media;
import pl.mwasyluk.ouroom_server.domain.user.User;

public class ChatSendableFactory {
    public @NonNull ChatSendable create(@NonNull User creator, String message, Media media) {
        return new ChatSendable(creator, message, media);
    }
}
