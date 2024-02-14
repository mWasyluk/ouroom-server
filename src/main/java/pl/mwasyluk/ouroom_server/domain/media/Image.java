package pl.mwasyluk.ouroom_server.domain.media;

import java.io.IOException;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import jakarta.persistence.Entity;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
public class Image extends BaseMedia {
    protected Image(byte @NonNull [] bytes) throws IOException {
        super(bytes);
    }

    protected Image(@NonNull String url) throws IOException {
        super(url);
    }

    @Override
    protected void validate() {
        super.validate();
        if (!MediaUtils.isImageType(this.getType())) {
            throw new IllegalArgumentException(
                    "Cannot instantiate Image object with " + this.getType() + " media type.");
        }
    }
}
