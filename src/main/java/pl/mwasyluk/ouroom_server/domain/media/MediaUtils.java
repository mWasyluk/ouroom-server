package pl.mwasyluk.ouroom_server.domain.media;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
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

    public static Media emptyMockOf(MediaType mediaType) {
        if (isImageType(mediaType)) {
            Image image = new Image();
            image.content = "mock".getBytes();
            image.type = mediaType;
            return image;
        }

        if (isVideoType(mediaType)) {
            Video video = new Video();
            video.content = "mock".getBytes();
            video.type = mediaType;
            return video;
        }

        throw new IllegalArgumentException("MediaType " + mediaType + " is not supported.");
    }

    public static boolean isMediaTypeSupported(MediaType mediaType) {
        return SUPPORTED_MEDIA_TYPES.contains(mediaType);
    }

    public static boolean isImageType(@NonNull MediaType mediaType) {
        return SUPPORTED_IMAGE_TYPES.contains(mediaType);
    }

    public static boolean isVideoType(@NonNull MediaType mediaType) {
        return SUPPORTED_VIDEO_TYPES.contains(mediaType);
    }

    public static Media of(byte[] content) throws IOException {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Cannot instantiate Media object with empty content.");
        }

        MediaType mediaType = guessMediaType(content);

        if (isImageType(mediaType)) {
//            return new Image(type, content);
            return new Image(content);
        }

        if (isVideoType(mediaType)) {
//            return new Video(type, content);
            return new Video(content);
        }

        throw new IllegalArgumentException("MediaType " + mediaType + " is not supported.");
    }

    public static Media of(@NonNull String url) throws IOException {
        MediaType mediaType = fetchMediaType(url);

        if (isImageType(mediaType)) {
//            return new Image(type, content);
            return new Image(url);
        }

        if (isVideoType(mediaType)) {
//            return new Video(type, content);
            return new Video(url);
        }

        throw new IllegalArgumentException("MediaType " + mediaType + " is not supported.");
    }

    public static Media from(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        return of(file.getBytes());
    }

    public static MediaType guessMediaType(byte[] content) throws IOException {
        return MediaType.parseMediaType(
                URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(content)));
    }

    public static MediaType fetchMediaType(String url) throws IOException {
        BufferedInputStream inputStream = new BufferedInputStream(new URL(url).openStream());
        String mimeType = URLConnection.guessContentTypeFromStream(inputStream);
        inputStream.close();
        return MediaType.parseMediaType(mimeType);
    }

    public static byte[] fetchContent(String url) throws IOException {
        BufferedInputStream inputStream = new BufferedInputStream(new URL(url).openStream());
        byte[] content = inputStream.readAllBytes();
        inputStream.close();
        return content;
    }
}
