package pl.mwasyluk.ouroom_server.newdomain.container;

import java.util.UUID;

import lombok.NonNull;

public interface Conversation extends Membership, SendablesContainer {
    boolean isAdminByUserId(@NonNull UUID userId);
    boolean hasAnyAdmin();
}
