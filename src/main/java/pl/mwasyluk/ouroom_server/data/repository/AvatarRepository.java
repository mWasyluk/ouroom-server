package pl.mwasyluk.ouroom_server.data.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pl.mwasyluk.ouroom_server.domain.userdetails.ProfileAvatar;

@Repository
public interface AvatarRepository extends JpaRepository<ProfileAvatar, UUID> {

}
