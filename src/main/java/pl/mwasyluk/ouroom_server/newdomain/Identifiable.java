package pl.mwasyluk.ouroom_server.newdomain;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.stereotype.Component;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.mwasyluk.ouroom_server.util.LoggerUtils;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Component

@MappedSuperclass
public abstract class Identifiable implements Serializable {
    public static boolean idEquals(Identifiable i1, Identifiable i2) {
        if (i1 == null) {
            return false;
        }
        return i1.idEquals(i2);
    }

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    @Transient
    protected final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    @Transient
    protected final LoggerUtils logUtils = new LoggerUtils();

    @NonNull
    @Id
    @Setter(AccessLevel.PRIVATE)
    private UUID id = UUID.randomUUID();

    public boolean idEquals(Identifiable i) {
        if (i == null) {
            return false;
        }
        return id.equals(i.getId());
    }
}
