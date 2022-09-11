package pl.wasyluva.spring_messengerapi.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserProfile;

import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
}
