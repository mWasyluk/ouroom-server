package pl.mwasyluk.ouroom_server.domain.media;

import org.springframework.http.MediaType;

public interface VideoMediaType {
    MediaType MP4 = MediaType.parseMediaType("video/mp4");
    MediaType MPEG = MediaType.parseMediaType("video/mpeg");
}
