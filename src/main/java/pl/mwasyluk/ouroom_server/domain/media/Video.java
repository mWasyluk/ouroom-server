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
public class Video extends BaseMedia {
    protected Video(byte @NonNull [] content) throws IOException {
        super(content);
    }

    protected Video(@NonNull String url) throws IOException {
        super(url);
    }

    @Override
    protected void validate() {
        super.validate();
        if (!MediaUtils.isImageType(this.getType())) {
            throw new IllegalArgumentException(
                    "Cannot instantiate Video object with " + this.getType() + " media type.");
        }
    }
}
