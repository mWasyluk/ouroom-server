package pl.mwasyluk.ouroom_server.domain.media.source;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import lombok.NonNull;

public interface DataSource {
    static DataSource of(@NonNull URL url) {
        return new ExternalDataSource(url);
    }
    static DataSource of(@NonNull String url) throws MalformedURLException {
        return of(new URL(url));
    }
    static DataSource of(byte @NonNull [] content) {
        return new InternalDataSource(content);
    }

    InputStream getInputStream();
    String getContentType();
    default byte[] getData() {
        try {
            return this.getInputStream().readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
