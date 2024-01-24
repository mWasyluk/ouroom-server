package pl.mwasyluk.ouroom_server.newdomain;

import org.springframework.context.annotation.Configuration;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;

import pl.mwasyluk.ouroom_server.newdomain.media.Image;

@Data
@Setter(AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Configuration
@MappedSuperclass
public abstract class Presentable extends Identifiable {
    protected String name;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    protected Image image;
}
