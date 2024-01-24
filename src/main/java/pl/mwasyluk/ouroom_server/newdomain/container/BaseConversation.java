package pl.mwasyluk.ouroom_server.newdomain.container;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import jakarta.persistence.MappedSuperclass;

import pl.mwasyluk.ouroom_server.newdomain.Presentable;
import pl.mwasyluk.ouroom_server.newdomain.media.Image;

@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

@MappedSuperclass
public abstract class BaseConversation extends Presentable implements Conversation {

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
