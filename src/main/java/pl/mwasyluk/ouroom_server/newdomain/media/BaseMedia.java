package pl.mwasyluk.ouroom_server.newdomain.media;

import org.springframework.http.MediaType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.Entity;
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

    @Setter(AccessLevel.PRIVATE)
    @ToString.Include
    private @NonNull MediaType type;

    @Setter(AccessLevel.PRIVATE)
    private byte @NonNull [] content;

    protected BaseMedia(@NonNull MediaType type, byte @NonNull [] content) {
        this.type = type;
        this.content = content;
        validate();
    }

    protected void validate() {
        if (getContentSize() <= 0) {
            throw new IllegalArgumentException("Cannot instantiate Media object with empty content.");
        }
    }

    @Override
    @ToString.Include(name = "contentSize")
    public int getContentSize() {
        return content.length;
    }
}
