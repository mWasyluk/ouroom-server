package pl.mwasyluk.ouroom_server.repos;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import pl.mwasyluk.ouroom_server.domain.user.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @Query("""
            SELECT
                COUNT(*)
            FROM User u
            WHERE u.id in ?1
            """)
    int countAllByIdIn(Set<UUID> userIdSet);
    default boolean allExistByIdIn(Set<UUID> userIdSet) {
        if (userIdSet == null || userIdSet.isEmpty()) {
            return true;
        }
        return countAllByIdIn(userIdSet) == userIdSet.size();
    }

    @Query(value = "SELECT u FROM User u WHERE u.account.username = ?1")
    Optional<User> findByUsername(String email);
}
