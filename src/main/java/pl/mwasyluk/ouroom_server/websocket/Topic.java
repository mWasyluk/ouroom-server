package pl.mwasyluk.ouroom_server.websocket;

import lombok.Getter;

public enum Topic {
    MESSAGES("messages"), MEMBERSHIPS("memberships");

    @Getter
    private final String value;

    Topic(String value) {
        this.value = value;
    }
}
