package pl.mwasyluk.ouroom_server.dto.member;

import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import lombok.Data;

import pl.mwasyluk.ouroom_server.domain.member.MemberPrivilege;

@Data
public class MembersForm {
    private UUID membershipId;
    private Map<UUID, EnumSet<MemberPrivilege>> members;
}
