package pl.mwasyluk.ouroom_server.newdomain.media;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.boot.web.server.MimeMappings;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static pl.mwasyluk.ouroom_server.util.LoggerUtils.operationFailedDueTo;

public interface Media {
    Logger log = LoggerFactory.getLogger(Media.class);

    Set<MediaType> SUPPORTED_MEDIA_TYPES = new HashSet<>() {{
        addAll(Image.SUPPORTED_MEDIA_TYPES);
        addAll(Video.SUPPORTED_MEDIA_TYPES);
    }};

    static Media of(MediaType type, byte[] content) {
        if (type == null || content == null || content.length == 0) {
            throw new IllegalArgumentException("Cannot instantiate Media without a MediaType or non-empty content.");
        }

        if (Image.SUPPORTED_MEDIA_TYPES.contains(type)) {
            return new Image(type, content);
        }

        if (Video.SUPPORTED_MEDIA_TYPES.contains(type)) {
            return new Video(type, content);
        }

        throw new IllegalArgumentException("MediaType " + type + " is not supported.");
    }

    static Media from(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getOriginalFilename() == null) {
            log.debug(operationFailedDueTo("multipart file conversion",
                    "lack of the file name to determine the extension", file));
            return null;
        }

        String[] split = file.getOriginalFilename().split("\\.");
        String mimeType = MimeMappings.DEFAULT.get(split[split.length - 1]);

        if (mimeType == null) {
            log.debug(operationFailedDueTo("multipart file conversion",
                    "invalid MIME type resolved based on the file extension", file));
            return null;
        }

        MediaType mediaType = MediaType.parseMediaType(mimeType);
        Media media = null;

        try {
            media = Media.of(mediaType, file.getBytes());
        } catch (IOException e) {
            log.debug(operationFailedDueTo("multipart file conversion", "IOException while reading byte array", file));
        }

        return media;
    }

    @NonNull UUID getId();
    @NonNull MediaType getType();
    byte @NonNull [] getContent();
    int getContentSize();
}
