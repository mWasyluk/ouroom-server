package pl.mwasyluk.ouroom_server.repos;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import pl.mwasyluk.ouroom_server.domain.member.ChatMember;
import pl.mwasyluk.ouroom_server.domain.member.id.MemberId;

@Repository
public interface MemberRepository extends JpaRepository<ChatMember, MemberId> {
    // Custom JPQL approach
    @Query("""
            SELECT COUNT(*) > 0
            FROM ChatMember m
            WHERE m.memberId.membership.id = ?2 AND m.memberId.user.id in ?1
            """)
    boolean anyExists(Set<UUID> userIds, UUID membershipId);

    // Facade approach using Derived Query Methods
    List<ChatMember> findAllByMemberIdUserIdInAndMemberIdMembershipId(Set<UUID> userIds, UUID membershipId);
    // facade method
    default List<ChatMember> findAllByUserIdIn(Set<UUID> userIds, UUID membershipId) {
        return findAllByMemberIdUserIdInAndMemberIdMembershipId(userIds, membershipId);
    }

    List<ChatMember> findAllByMemberIdMembershipId(UUID membershipId);
    // facade method
    default List<ChatMember> findAllByMembershipId(UUID membershipId) {
        return findAllByMemberIdMembershipId(membershipId);
    }
}
