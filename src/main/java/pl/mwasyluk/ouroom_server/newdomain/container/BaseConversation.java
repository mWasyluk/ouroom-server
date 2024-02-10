package pl.mwasyluk.ouroom_server.newdomain.container;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;

import pl.mwasyluk.ouroom_server.newdomain.Identifiable;
import pl.mwasyluk.ouroom_server.newdomain.Presentable;
import pl.mwasyluk.ouroom_server.newdomain.media.Image;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

@MappedSuperclass
public abstract class BaseConversation extends Identifiable implements Conversation, Presentable {
    private String name;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Image image;
}
