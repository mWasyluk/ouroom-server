package pl.mwasyluk.ouroom_server.domain.media;

import java.io.IOException;

import org.springframework.http.MediaType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import pl.mwasyluk.ouroom_server.domain.Identifiable;
import pl.mwasyluk.ouroom_server.util.LoggerUtils;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "media")
public abstract class BaseMedia extends Identifiable implements Media {
    @Setter(AccessLevel.PRIVATE)
    protected String url;

    @Setter(AccessLevel.PRIVATE)
    protected @NonNull MediaType type;

    @Setter(AccessLevel.PRIVATE)
    @ToString.Exclude
    @Lob
    @Column(length = 5120)
    protected byte @NonNull [] content;

    @Transient
    private boolean requiresFetch = false;

    //    protected BaseMedia(@NonNull MediaType type, byte @NonNull [] content) {
    protected BaseMedia(byte @NonNull [] content) throws IOException {
        this.type = MediaUtils.guessMediaType(content);
        this.content = content;
        validate();
    }

    protected BaseMedia(@NonNull String url) throws IOException {
        this.url = url;
        this.type = MediaUtils.fetchMediaType(url);
        this.content = new byte[0];
        validate();
    }

    protected void validate() {
        if (getContentSize() < 1) {
            if (url == null || url.isBlank()) {
                throw new IllegalArgumentException("Cannot instantiate Media object with empty content and url.");
            }
            requiresFetch = true;
        }
    }

    @PrePersist
    private void prePersist() {
        if (url != null) {
            content = new byte[0];
        }
    }

    public byte @NonNull [] getContent() {
        if (requiresFetch) {
            try {
                content = MediaUtils.fetchContent(url);
                requiresFetch = false;
            } catch (IOException e) {
                log.error(LoggerUtils.operationFailedDueTo("fetch content",
                        "fetching exception while the instance requires fetch"), this);
            }
        }
        return content;
    }

    @Override
    @ToString.Include(name = "contentSize")
    public int getContentSize() {
        return content.length;
    }
}
