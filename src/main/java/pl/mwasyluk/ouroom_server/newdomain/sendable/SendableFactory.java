package pl.mwasyluk.ouroom_server.newdomain.sendable;

import lombok.NonNull;

import pl.mwasyluk.ouroom_server.newdomain.media.Media;
import pl.mwasyluk.ouroom_server.newdomain.user.User;

public interface SendableFactory {
    @NonNull Sendable createTextSendable(@NonNull User creator, @NonNull String message);
    @NonNull Sendable createMediaSendable(@NonNull User creator, @NonNull Media media, String description);
}
