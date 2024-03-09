package pl.mwasyluk.ouroom_server.repos;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pl.mwasyluk.ouroom_server.domain.sendable.ChatSendable;

@Repository
public interface SendableRepository extends JpaRepository<ChatSendable, UUID> {
    List<ChatSendable> findAllByContainerId(UUID containerId);
}
