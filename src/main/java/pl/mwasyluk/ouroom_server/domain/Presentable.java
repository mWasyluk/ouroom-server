package pl.mwasyluk.ouroom_server.domain;

import pl.mwasyluk.ouroom_server.domain.media.Image;

public interface Presentable {
    String getName();
    Image getImage();
}
