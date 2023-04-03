package pl.mwasyluk.ouroom_server.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pl.mwasyluk.ouroom_server.domain.message.Conversation;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    List<Conversation> findByParticipatorsIdIn(List<UUID> participatorsId);

    List<Conversation> findAllByParticipatorsId(UUID participatorId);
}
