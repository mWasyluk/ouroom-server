package pl.mwasyluk.ouroom_server.domain.media.source;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import pl.mwasyluk.ouroom_server.exceptions.ConversionException;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class ExternalDataSource implements DataSource {
    @Setter(AccessLevel.PRIVATE)
    private URL url = null;

    protected ExternalDataSource(@NonNull URL url) {
        this.url = url;
    }

    @Override
    public InputStream getInputStream() {
        try {
            return url.openStream();
        } catch (IOException e) {
            throw new ConversionException("Data stream from URL '" + url + "' could not be opened");
        }
    }

    @Override
    public String getContentType() {
        try {
            String contentType = url.openConnection().getContentType();
            return contentType != null ? contentType : "application/octet-stream";
        } catch (IOException e) {
            return null;
        }
    }
}
