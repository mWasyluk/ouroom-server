package pl.mwasyluk.ouroom_server.domain.media;

import java.util.UUID;

import org.springframework.http.MediaType;
import lombok.NonNull;

public interface Media {
    @NonNull UUID getId();
    String getUrl();
    @NonNull MediaType getType();
    byte @NonNull [] getContent();
    int getContentSize();
}
