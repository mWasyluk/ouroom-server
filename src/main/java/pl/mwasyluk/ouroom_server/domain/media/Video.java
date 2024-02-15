package pl.mwasyluk.ouroom_server.domain.media;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import jakarta.persistence.Entity;

import pl.mwasyluk.ouroom_server.domain.media.source.DataSource;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
public class Video extends BaseMedia {
    protected Video(@NonNull DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected void validate() {
        if (!MediaUtils.isImageType(this.getType())) {
            throw new IllegalArgumentException(
                    "Cannot instantiate Video object with " + this.getType() + " media type.");
        }
    }
}
