package pl.mwasyluk.ouroom_server.domain.media.source;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import pl.mwasyluk.ouroom_server.exceptions.InitializationException;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InternalDataSource implements DataSource {
    @Setter(AccessLevel.PRIVATE)
    private byte[] data;

    protected InternalDataSource(byte @NonNull [] data) {
        if (data.length == 0) {
            throw new InitializationException("Cannot instantiate InternalDataSource with an empty bytes array");
        }
        this.data = data;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(data);
    }

    @Override
    public String getContentType() {
        try {
            return URLConnection.guessContentTypeFromStream(getInputStream());
        } catch (IOException e) {
            return null;
        }
    }
}
