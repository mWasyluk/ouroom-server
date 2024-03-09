package pl.mwasyluk.ouroom_server.services.member;

import java.util.Collection;
import java.util.UUID;

import lombok.NonNull;

import pl.mwasyluk.ouroom_server.dto.member.MemberPresentableView;
import pl.mwasyluk.ouroom_server.dto.member.MembersForm;

public interface MemberService {
    @NonNull Collection<MemberPresentableView> readAllInMembership(@NonNull UUID membershipId);
    @NonNull Collection<MemberPresentableView> createAll(@NonNull MembersForm membersForm);
    @NonNull Collection<MemberPresentableView> updateAll(@NonNull MembersForm membersForm);
    void delete(@NonNull MembersForm membersForm);
}
