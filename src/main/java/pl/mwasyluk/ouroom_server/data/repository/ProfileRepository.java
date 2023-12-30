package pl.mwasyluk.ouroom_server.data.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pl.mwasyluk.ouroom_server.domain.userdetails.Profile;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    List<Profile> findAllByFirstNameStartsWithIgnoreCaseAndLastNameStartsWithIgnoreCase(String firstNameQuery,
            String lastNameQuery);
}
