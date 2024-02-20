package pl.mwasyluk.ouroom_server.domain.container;

import java.util.UUID;

import lombok.NonNull;

import pl.mwasyluk.ouroom_server.domain.user.User;

public interface Conversation extends Membership, SendablesContainer {
    User getOwner();
    boolean isAdminByUserId(@NonNull UUID userId);
}
