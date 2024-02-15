package pl.mwasyluk.ouroom_server.domain.media;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.http.MediaType;
import lombok.NonNull;

public class MediaUtils {
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

    public static boolean isMediaTypeSupported(MediaType mediaType) {
        return SUPPORTED_MEDIA_TYPES.contains(mediaType);
    }

    public static boolean isImageType(@NonNull MediaType mediaType) {
        return SUPPORTED_IMAGE_TYPES.contains(mediaType);
    }

    public static boolean isVideoType(@NonNull MediaType mediaType) {
        return SUPPORTED_VIDEO_TYPES.contains(mediaType);
    }

    public static MediaType guessMediaType(byte[] content) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        String mimeType = URLConnection.guessContentTypeFromStream(inputStream);
        inputStream.close();
        return MediaType.parseMediaType(mimeType);
    }

    public static MediaType fetchMediaType(String url) throws IOException {
        return fetchMediaType(new URL(url));
    }

    public static MediaType fetchMediaType(URL url) throws IOException {
        return MediaType.parseMediaType(url.openConnection().getContentType());
    }

    public static byte[] fetchContent(String url) throws IOException {
        return fetchContent(new URL(url));
    }

    public static byte[] fetchContent(URL url) throws IOException {
        BufferedInputStream inputStream = new BufferedInputStream(url.openStream());
        byte[] content = inputStream.readAllBytes();
        inputStream.close();
        return content;
    }
}
