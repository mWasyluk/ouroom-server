package pl.mwasyluk.ouroom_server.domain.media;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import jakarta.persistence.Entity;

import pl.mwasyluk.ouroom_server.domain.media.source.DataSource;
import pl.mwasyluk.ouroom_server.exceptions.InitializationException;
import pl.mwasyluk.ouroom_server.utils.MediaUtil;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
public class Image extends Media {
    protected Image(@NonNull DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected void validate() {
        String contentType = getDataSource().getContentType();
        if (contentType == null || !MediaUtil.isImageType(contentType)) {
            throw new InitializationException(
                    "Cannot instantiate Image object with " + contentType + " media type.");
        }
    }
}
