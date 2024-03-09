package pl.mwasyluk.ouroom_server.repos;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pl.mwasyluk.ouroom_server.domain.media.Media;

@Repository
public interface MediaRepository extends JpaRepository<Media, UUID> {
}
