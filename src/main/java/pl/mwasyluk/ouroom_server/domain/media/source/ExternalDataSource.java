package pl.mwasyluk.ouroom_server.domain.media.source;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

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
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getContentType() {
        URLConnection urlConnection;
        try {
            urlConnection = url.openConnection();
        } catch (IOException e) {
            return null;
        }

        String contentType = urlConnection.getContentType();
        return contentType != null ? contentType : "application/octet-stream";
    }
}
