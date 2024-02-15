package pl.mwasyluk.ouroom_server.domain.media;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import lombok.NonNull;

import pl.mwasyluk.ouroom_server.domain.media.source.DataSource;

public interface Media {
    static Media of(@NonNull DataSource dataSource) {
        MediaType mediaType = MediaType.parseMediaType(dataSource.getContentType());
        if (MediaUtils.isImageType(mediaType)) {
            return new Image(dataSource);
        }
        if (MediaUtils.isVideoType(mediaType)) {
            return new Video(dataSource);
        }

        throw new IllegalArgumentException("MediaType " + mediaType + " is not supported.");
    }

    static Media from(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        return of(DataSource.of(file.getBytes()));
    }

    @NonNull UUID getId();
    @NonNull MediaType getType();
    @NonNull DataSource getSource();
}
