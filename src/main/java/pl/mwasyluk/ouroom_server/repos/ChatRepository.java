package pl.mwasyluk.ouroom_server.repos;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import pl.mwasyluk.ouroom_server.domain.container.Chat;
import pl.mwasyluk.ouroom_server.dto.chat.ChatDetailsView;

@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {
    @Query("""
            SELECT c
            FROM Chat c
            LEFT JOIN FETCH ChatMember m on m.memberId.membership.id = c.id
            WHERE m.memberId.user.id = ?1
            """)
    Collection<Chat> findAllByUserId(UUID userId);

    @Query("""
            SELECT new pl.mwasyluk.ouroom_server.dto.chat.ChatDetailsView(
                c.id,
                c.name,
                c.image.id,
                SIZE(c.members),
                SIZE(c.sendables)
            )
            FROM Chat c
            WHERE c.id = ?1
            """)
    Optional<ChatDetailsView> findDetailsById(UUID chatId);
}
