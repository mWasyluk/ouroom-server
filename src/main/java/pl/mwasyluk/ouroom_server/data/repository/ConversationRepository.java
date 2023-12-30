package pl.mwasyluk.ouroom_server.data.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pl.mwasyluk.ouroom_server.domain.message.Conversation;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    List<Conversation> findByParticipatorsIdIn(List<UUID> participatorsId);

    List<Conversation> findAllByParticipatorsId(UUID participatorId);
}
