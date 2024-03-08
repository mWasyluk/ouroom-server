package pl.mwasyluk.ouroom_server.utils;

import java.util.HashSet;
import java.util.Set;

import org.springframework.http.MediaType;
import lombok.NonNull;

import pl.mwasyluk.ouroom_server.domain.media.VideoMediaType;

public class MediaUtil {
    public static final Set<MediaType> SUPPORTED_IMAGE_TYPES = Set.of(
            MediaType.IMAGE_JPEG,
            MediaType.IMAGE_PNG,
            MediaType.IMAGE_GIF
    );
    public static final Set<MediaType> SUPPORTED_VIDEO_TYPES = Set.of(
            VideoMediaType.MP4,
            VideoMediaType.MPEG
    );
    public static final Set<MediaType> SUPPORTED_MEDIA_TYPES = new HashSet<>() {{
        addAll(SUPPORTED_IMAGE_TYPES);
        addAll(SUPPORTED_VIDEO_TYPES);
    }};

    public static boolean isMimeTypeSupported(String mimeType) {
        return mimeType != null && SUPPORTED_MEDIA_TYPES.stream().anyMatch(t -> t.toString().equals(mimeType));
    }

    public static boolean isImageType(@NonNull String mimeType) {
        return SUPPORTED_IMAGE_TYPES.stream().anyMatch(t -> t.toString().equals(mimeType));
    }

    public static boolean isVideoType(@NonNull String mimeType) {
        return SUPPORTED_VIDEO_TYPES.stream().anyMatch(t -> t.toString().equals(mimeType));
    }
}
