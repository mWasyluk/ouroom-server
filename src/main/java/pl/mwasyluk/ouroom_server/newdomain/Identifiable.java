package pl.mwasyluk.ouroom_server.newdomain;

import java.io.Serializable;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@MappedSuperclass
public abstract class Identifiable implements Serializable {
    @NonNull
    @Id
    @Setter(AccessLevel.PROTECTED)
    private UUID id = UUID.randomUUID();
}
