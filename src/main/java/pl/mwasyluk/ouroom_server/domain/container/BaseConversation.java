package pl.mwasyluk.ouroom_server.domain.container;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;

import pl.mwasyluk.ouroom_server.domain.Identifiable;
import pl.mwasyluk.ouroom_server.domain.Presentable;
import pl.mwasyluk.ouroom_server.domain.media.Image;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

@MappedSuperclass
public abstract class BaseConversation extends Identifiable implements Conversation, Presentable {
    private String name;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Image image;

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            this.name = null;
        } else {
            this.name = name.trim();
        }
    }
}
