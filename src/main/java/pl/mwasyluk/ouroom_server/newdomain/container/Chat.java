package pl.mwasyluk.ouroom_server.newdomain.container;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import pl.mwasyluk.ouroom_server.newdomain.member.ChatMember;
import pl.mwasyluk.ouroom_server.newdomain.member.Member;
import pl.mwasyluk.ouroom_server.newdomain.member.MemberPrivilege;
import pl.mwasyluk.ouroom_server.newdomain.sendable.ChatSendable;
import pl.mwasyluk.ouroom_server.newdomain.sendable.Sendable;
import pl.mwasyluk.ouroom_server.newdomain.user.User;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
public class Chat extends BaseConversation {
    public static final EnumSet<MemberPrivilege> ADMIN_PRIVILEGES
            = EnumSet.copyOf(Arrays.asList(MemberPrivilege.values()));
    public static final EnumSet<MemberPrivilege> MEMBER_PRIVILEGES
            = EnumSet.of(MemberPrivilege.ADD_MESSAGES);

    @NonNull
    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true,
               targetEntity = ChatMember.class, mappedBy = "membership")
    protected Set<Member> members = new HashSet<>();

    protected int adminsAmount = 0;
    protected int allMembersAmount = 0;
    protected int sendables_amount = 0;

    @NonNull
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    @ToString.Exclude
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL,
               targetEntity = ChatSendable.class, mappedBy = "container")
    protected Set<Sendable> sendables = new HashSet<>();

    public Chat(@NonNull Set<Member> members) {
        members.forEach(this::safelyAddMember);

        if (adminsAmount <= 0) {
            throw new IllegalArgumentException("Conversation cannot be initialized without at least one administrator");
        }
    }

    /**
     <b>Safely adds</b> the given {@link Member} instance to this chat.
     <br>This method <b>ensures the appropriate admin management</b> and should always be used to update privileges.

     @param m
     {@link Member} instance to be added to this chat;

     @return true - if the given element has been updated;
     <br>    false - if the given element is the last present admin and new privileges do not match the appropriate
     values due to {@link #hasAdminPrivileges(Member)};

     @see Member#updatePrivileges(Member)
     */
    private boolean safelyAddMember(@NonNull Member m) {
        boolean isAdded = members.add(m);

        if (!isAdded) {
            return false;
        }

        if (!m.setMembership(this)) {
            log.error("The member has been added to this chat but its membership could not be overwritten. "
                      + logUtils.somethingUnexpectedAndInvolved(this, m));
            members.remove(m);
            return false;
        }

        if (hasAdminPrivileges(m)) {
            adminsAmount++;
        }
        allMembersAmount++;
        return true;
    }

    /**
     <b>Safely updates</b> the given {@link Member} instance with the privileges of another instance.
     <br>This method <b>ensures the appropriate admin management</b> and should always be used to update privileges.

     @param current
     {@link Member} instance to be updated with new collection of {@link MemberPrivilege} enum values;
     @param withNew
     {@link Member} instance with collection of {@link MemberPrivilege} enum values to be applied;

     @return true - if the given element has been updated;
     <br>    false - if the given element is the last present admin and new privileges do not match the appropriate
     values due to {@link #hasAdminPrivileges(Member)};

     @see Member#updatePrivileges(Member)
     */
    private boolean safelyUpdateMemberPrivileges(@NonNull Member current, @NonNull Member withNew) {
        if (!hasAdminPrivileges(current)) {
            boolean isUpdated = current.updatePrivileges(withNew);
            if (!isUpdated) {
                log.error("The given current member could not be updated with the given new state. ");
                return false;
            }

            adminsAmount += hasAdminPrivileges(withNew) ? 1 : 0;
            return true;
        }

        if (adminsAmount <= 1) {
            return false;
        }

        return current.updatePrivileges(withNew);
    }

    /**
     <b>Safely removes</b> the given member from this chat.
     <br>This method <b>ensures the appropriate admin management</b> and should always be used to remove a member.

     @param m
     {@link Member} instance to be removed from this chat;

     @return true - if the given element no longer belongs to this chat;
     <br>    false - if the given element is the last present admin of this chat;
     */
    private boolean safelyRemoveMember(@NonNull Member m) {
        if (hasAdminPrivileges(m) && adminsAmount <= 1) {
            return false;
        }

        if (!members.remove(m)) {
            return true;
        }
        allMembersAmount--;

        if (hasAdminPrivileges(m)) {
            adminsAmount--;
        }
        m.setMembership(null);
        return true;
    }

    private boolean hasAdminPrivileges(@NonNull Member member) {
        return member.hasPrivileges(ADMIN_PRIVILEGES);
    }

    public @NonNull Stream<Sendable> getSendableStreamById(@NonNull UUID sendableId) {
        return sendables.stream()
                .filter(s -> s.getId().equals(sendableId))
                .limit(1);
    }

    @Override
    public boolean isAdminByUserId(@NonNull UUID userId) {
        return getMemberByUserId(userId)
                .map(this::hasAdminPrivileges)
                .orElse(false);
    }

    @Override
    public @NonNull Set<Member> getAllMembers() {
        return Collections.unmodifiableSet(members);
    }

    // TODO {minor}: provide getStream method similar to the one provided for sendables
    @Override
    public Optional<Member> getMemberById(@NonNull UUID memberId) {
        return members.stream()
                .filter(m -> m.getId().equals(memberId))
                .findAny();
    }

    // TODO {minor}: provide getStream method similar to the one provided for sendables
    @Override
    public Optional<Member> getMemberByUserId(@NonNull UUID userId) {
        return members.stream()
                .filter(m -> m.getUser().getId().equals(userId))
                .findAny();
    }

    /**
     Adds the given member to this chat or replaces the present one with the given instance if it has the same user
     component, but different privileges. The method does not allow to override privileges of the last present admin. To
     verify

     @param member
     element to be put to this chat

     @return true - if element has been added or the same user with the same privileges is already a member of this chat
     <br>    false - if the same user is the last present admin and the new privileges do not match an admin
     */
    @Override
    public boolean putMember(@NonNull Member member) {
        Optional<Member> om = getMemberById(member.getId());
        if (om.isEmpty()) {
            return safelyAddMember(member);
        }

        Member presentMember = om.get();
        boolean arePrivilegesDifferent = !presentMember.getPrivileges().equals(member.getPrivileges());
        return arePrivilegesDifferent && safelyUpdateMemberPrivileges(presentMember, member);
    }

    @Override
    public boolean removeMemberById(@NonNull UUID memberId) {
        return getMemberById(memberId)
                .map(this::safelyRemoveMember)
                .orElse(true);
    }

    /**
     @param userId
     id of the {@link User} component of the member instance to be removed from this chat.

     @return true - if the given element no longer belongs to this chat
     <br>    false - if the given element is the last present admin of this chat
     */
    @Override
    public boolean removeMemberByUserId(@NonNull UUID userId) {
        return getMemberByUserId(userId)
                .map(this::safelyRemoveMember)
                .orElse(true);
    }

    @Override
    public @NonNull Set<Sendable> getAllSendables() {
        return Collections.unmodifiableSet(sendables);
    }

    @Override
    public @NonNull Optional<Sendable> getSendableById(@NonNull UUID sendableId) {
        return getSendableStreamById(sendableId).findAny();
    }

    /**
     <b>Safely adds</b> the given {@link Sendable} instance to this chat <b>or updates</b> the present instance with the
     same id.
     <br>This method <b>ensures the appropriate sendables management</b>.

     @param sendable
     {@link Sendable} instance to be applied;

     @return true - if the given instance has been added or the present one has been updated;
     <br>    false - if the given instance could not be added or updated;

     @see Sendable#updateMutableFieldsWith(Sendable)
     */
    @Override
    public boolean putSendable(@NonNull Sendable sendable) {
        log.error("test: Reversing add operation due to overwrite problem with the given sendable's container. "
                  + logUtils.somethingUnexpectedAndInvolved(this, sendable));
        if (sendables.add(sendable)) {
            if (!sendable.setContainer(this)) {
                log.error("Reversing add operation due to overwrite problem with the given sendable's container. "
                          + logUtils.somethingUnexpectedAndInvolved(this, sendable));
                sendables.remove(sendable);
                return false;
            }

            sendables_amount++;
            return true;
        }

        Optional<Sendable> matchingOptional = getSendableStreamById(sendable.getId()).findAny();
        if (matchingOptional.isEmpty()) {
            log.error("Sendable cannot be added but also this Chat instance does not contain a sendable object with"
                      + " the same id. " + logUtils.somethingUnexpectedAndInvolved(this, sendable));
            return false;
        }

        Sendable presentInstance = matchingOptional.get();
        presentInstance.updateMutableFieldsWith(sendable);
        return true;
    }

    /**
     <b>Safely removes</b> the given {@link Sendable} instance from this chat.
     <br>This method <b>ensures the appropriate sendables management</b>.

     @param sendable
     {@link Sendable} instance to be removed;

     @return true - if the given instance is no longer present;
     <br>    false - if the given instance could be removed but due to the overwrite problem with the given instance the
     operation has been reversed;
     */
    @Override
    public boolean removeSendable(@NonNull Sendable sendable) {
        if (sendables.remove(sendable)) {
            if (!sendable.setContainer(this)) {
                log.error("Reversing add operation due to overwrite problem with the given sendable's container."
                          + logUtils.somethingUnexpectedAndInvolved(this, sendable));
                sendables.remove(sendable);
                return false;
            }
            sendables_amount--;
        }
        return true;
    }

    @Override
    public boolean removeSendableById(@NonNull UUID sendableId) {
        return getSendableById(sendableId).map(this::removeSendable).orElse(false);
    }
}
