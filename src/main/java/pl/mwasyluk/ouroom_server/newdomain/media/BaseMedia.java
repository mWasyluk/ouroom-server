package pl.mwasyluk.ouroom_server.newdomain.media;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import pl.mwasyluk.ouroom_server.newdomain.Identifiable;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "media")
public abstract class BaseMedia extends Identifiable implements Media {

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    @Enumerated(EnumType.STRING)
    private MediaGroup.Format format;

    @Setter(AccessLevel.PRIVATE)
    @ToString.Exclude
    private byte @NonNull [] bytes;

    public BaseMedia(@NonNull MediaGroup.Format format, byte @NonNull [] bytes) {
        this.format = format;
        this.bytes = bytes;
    }

    @Override
    @ToString.Include(name = "bytesSize")
    public int getBytesSize() {
        return bytes.length;
    }
}
