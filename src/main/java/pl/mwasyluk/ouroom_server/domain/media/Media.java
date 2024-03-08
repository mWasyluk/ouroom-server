package pl.mwasyluk.ouroom_server.domain.media;

import java.lang.reflect.Method;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Transient;

import pl.mwasyluk.ouroom_server.controllers.MediaController;
import pl.mwasyluk.ouroom_server.domain.Identifiable;
import pl.mwasyluk.ouroom_server.domain.media.source.DataSource;
import pl.mwasyluk.ouroom_server.exceptions.InitializationException;
import pl.mwasyluk.ouroom_server.exceptions.UnexpectedStateException;
import pl.mwasyluk.ouroom_server.utils.MediaUtil;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Media extends Identifiable {
    @Transient
    private static final Method internalUrlMethod;

    public static @NonNull Media of(@NonNull DataSource dataSource) {
        if (!MediaUtil.isMimeTypeSupported(dataSource.getContentType())) {
            throw new InitializationException("Mime type '" + dataSource.getContentType() + "' is not supported.");
        }
        if (MediaUtil.isImageType(dataSource.getContentType())) {
            return new Image(dataSource);
        }
        if (MediaUtil.isVideoType(dataSource.getContentType())) {
            return new Video(dataSource);
        }

        throw new UnexpectedStateException(
                "Mime type '" + dataSource.getContentType() + "' could not be matched with any available media type.");
    }

    static {
        try {
            internalUrlMethod = MediaController.class.getMethod("readById", UUID.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Setter(AccessLevel.PRIVATE)
    private DataSource dataSource;

    protected Media(@NonNull DataSource dataSource) {
        this.dataSource = dataSource;
        validate();
    }

    abstract protected void validate();

    public @NonNull MediaType getType() {
        return MediaType.parseMediaType(dataSource.getContentType());
    }

    public @NonNull DataSource getSource() {
        return dataSource;
    }

    public @NonNull String getInternalUrl() {
        return MvcUriComponentsBuilder
                .fromMethod(MediaController.class, internalUrlMethod, getId())
                .toUriString();
    }
}
