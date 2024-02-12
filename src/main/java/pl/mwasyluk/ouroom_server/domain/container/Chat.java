package pl.mwasyluk.ouroom_server.domain.container;

import java.util.*;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import pl.mwasyluk.ouroom_server.domain.member.ChatMember;
import pl.mwasyluk.ouroom_server.domain.member.ChatMemberFactory;
import pl.mwasyluk.ouroom_server.domain.member.Member;
import pl.mwasyluk.ouroom_server.domain.member.MemberPrivilege;
import pl.mwasyluk.ouroom_server.domain.sendable.ChatSendable;
import pl.mwasyluk.ouroom_server.domain.sendable.ChatSendableFactory;
import pl.mwasyluk.ouroom_server.domain.sendable.Sendable;
import pl.mwasyluk.ouroom_server.domain.user.User;

/**
 Chat is an entity that holds members and messages sent by them to that particular group.
 <br> A Chat handles member creation on its own at a client's request based on the given {@link User} and
 {@link MemberPrivilege} set.
 <br> It is different with {@link Sendable}s, the Chat receives ready-made instances, assign them to the Chat instance
 and stores.
 <br><B>Every chat instance should have at least one admin</B>. This can be helped by calling the
 {@link Chat#hasAnyAdmin()} method before persisting an entity.
 */
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
public class Chat extends BaseConversation {
    public static final ChatMemberFactory MEMBER_FACTORY = new ChatMemberFactory();
    public static final ChatSendableFactory SENDABLE_FACTORY = new ChatSendableFactory();

    public static final EnumSet<MemberPrivilege> ADMIN_PRIVILEGES
            = EnumSet.copyOf(Arrays.asList(MemberPrivilege.values()));
    public static final EnumSet<MemberPrivilege> DEFAULT_PRIVILEGES
            = EnumSet.of(MemberPrivilege.ADD_MESSAGES);

    @NonNull
    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true,
               targetEntity = ChatMember.class, mappedBy = "membership")
    protected Collection<Member> members = new ArrayList<>();

    @NonNull
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    @ToString.Exclude
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL,
               targetEntity = ChatSendable.class, mappedBy = "container")
    protected Collection<Sendable> sendables = new ArrayList<>();

    public Chat(@NonNull User adminUser, Map<User, Set<MemberPrivilege>> memberUserPrivilegesMap) {
        this.addMember(adminUser, Chat.ADMIN_PRIVILEGES);
        if (memberUserPrivilegesMap != null && memberUserPrivilegesMap.size() > 0) {
            memberUserPrivilegesMap.forEach(this::addMember);
        }
    }

    private boolean hasAdminPrivileges(@NonNull Member member) {
        return member.hasPrivileges(ADMIN_PRIVILEGES);
    }

    @Override
    public boolean isAdminByUserId(@NonNull UUID userId) {
        return getMemberByUserId(userId)
                .map(this::hasAdminPrivileges)
                .orElse(false);
    }

    @Override
    public boolean hasAnyAdmin() {
        return members.stream().anyMatch(this::hasAdminPrivileges);
    }

    @Override
    public @NonNull Collection<Member> getAllMembers() {
        return Collections.unmodifiableCollection(members);
    }

    @Override
    public Optional<Member> getMemberByUserId(@NonNull UUID userId) {
        return members.stream().filter(m -> m.getUser().getId().equals(userId)).findAny();
    }

    @Override
    public boolean addMember(@NonNull User user, Set<MemberPrivilege> privileges) {
        if (getMemberByUserId(user.getId()).isPresent()) {
            return false;
        }

        Member newMember = MEMBER_FACTORY.create(user, privileges);
        return members.add(newMember) && newMember.setMembership(this);
    }

    @Override
    public boolean removeMemberByUserId(@NonNull UUID userId) {
        return members.removeIf(m -> m.getUser().getId().equals(userId) && m.setMembership(null));
    }

    @Override
    public @NonNull Collection<Sendable> getAllSendables() {
        return Collections.unmodifiableCollection(sendables);
    }

    @Override
    public @NonNull Optional<Sendable> getSendableById(@NonNull UUID sendableId) {
        return sendables.stream().filter(s -> s.getId().equals(sendableId)).findAny();
    }

    @Override
    public boolean addSendable(@NonNull Sendable sendable) {
        sendables.add(sendable);
        sendable.setContainer(this);
        return true;
    }

    @Override
    public boolean removeSendableById(@NonNull UUID sendableId) {
        return sendables.removeIf(s -> s.getId().equals(sendableId) && s.setContainer(null));
    }
}
