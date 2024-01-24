package pl.mwasyluk.ouroom_server.newdomain.sendable;

import org.springframework.stereotype.Component;
import lombok.NonNull;

import pl.mwasyluk.ouroom_server.newdomain.media.Media;
import pl.mwasyluk.ouroom_server.newdomain.user.User;

@Component
public class ChatSendableFactory implements SendableFactory {
    @NonNull
    @Override
    public Sendable createTextSendable(@NonNull User creator, @NonNull String message) {
        return new ChatSendable(creator, message, null);
    }

    @NonNull
    @Override
    public Sendable createMediaSendable(@NonNull User creator, @NonNull Media media, String description) {
        return new ChatSendable(creator, description, media);
    }
}
