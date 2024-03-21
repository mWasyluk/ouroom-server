package pl.mwasyluk.ouroom_server.domain.media;

import org.springframework.http.MediaType;

public interface VideoMediaType {
    String VIDEO_MP4_VALUE = "video/mp4";
    String VIDEO_MPEG_VALUE = "video/mpeg";
    
    MediaType VIDEO_MP4 = MediaType.parseMediaType(VIDEO_MP4_VALUE);
    MediaType VIDEO_MPEG = MediaType.parseMediaType(VIDEO_MPEG_VALUE);
}
