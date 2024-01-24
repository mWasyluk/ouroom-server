package pl.mwasyluk.ouroom_server.newdomain.media;

import java.util.UUID;

import lombok.NonNull;

public interface Media {
    @NonNull UUID getId();

    @NonNull MediaGroup getMediaGroup();
    @NonNull MediaGroup.Format getFormat();

    byte @NonNull [] getBytes();
    int getBytesSize();
}
