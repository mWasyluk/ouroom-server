package pl.mwasyluk.ouroom_server.domain.sendable;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import pl.mwasyluk.ouroom_server.domain.container.Chat;
import pl.mwasyluk.ouroom_server.domain.container.SendablesContainer;
import pl.mwasyluk.ouroom_server.domain.user.User;
import pl.mwasyluk.ouroom_server.exceptions.InitializationException;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
@Table(indexes = {@Index(name = "containers_index", columnList = "container_id")})
public class ChatSendable extends BaseSendable {
    @ManyToOne(targetEntity = Chat.class)
    protected SendablesContainer container;

    public ChatSendable(@NonNull User creator, @NonNull String message) {
        super(creator, message);
    }

    @Override
    protected void initMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new InitializationException("Cannot initialize ChatSendable with an empty message.");
        }
        this.message = message.trim();
    }

    @Override
    public SendablesContainer getContainer() {
        return container;
    }

    @Override
    public boolean setContainer(@NonNull SendablesContainer container) {
        this.container = container;
        return true;
    }
}
