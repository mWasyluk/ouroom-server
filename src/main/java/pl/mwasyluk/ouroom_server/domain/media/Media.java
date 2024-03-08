package pl.mwasyluk.ouroom_server.domain.media;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import lombok.NonNull;

import pl.mwasyluk.ouroom_server.controllers.MediaController;
import pl.mwasyluk.ouroom_server.domain.media.source.DataSource;
import pl.mwasyluk.ouroom_server.exceptions.InitializationException;
import pl.mwasyluk.ouroom_server.exceptions.UnexpectedStateException;
import pl.mwasyluk.ouroom_server.utils.MediaUtil;

public interface Media {
    static @NonNull Media of(@NonNull DataSource dataSource) {
        if (!MediaUtil.isMimeTypeSupported(dataSource.getContentType())) {
            throw new InitializationException("Mime type '" + dataSource.getContentType() + "' is not supported.");
        }
        if (MediaUtil.isImageType(dataSource.getContentType())) {
            return new Image(dataSource);
        }
        if (MediaUtil.isVideoType(dataSource.getContentType())) {
            return new Video(dataSource);
        }

        throw new UnexpectedStateException(
                "Mime type '" + dataSource.getContentType() + "' could not be matched with any available media type.");
    }

    @NonNull UUID getId();
    @NonNull MediaType getType();
    @NonNull DataSource getSource();
}
