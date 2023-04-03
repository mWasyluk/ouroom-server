package pl.mwasyluk.ouroom_server.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pl.mwasyluk.ouroom_server.domain.userdetails.ProfileAvatar;

import java.util.UUID;

@Repository
public interface AvatarRepository extends JpaRepository<ProfileAvatar, UUID> {

}
