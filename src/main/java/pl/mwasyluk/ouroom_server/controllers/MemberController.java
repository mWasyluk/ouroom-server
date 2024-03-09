package pl.mwasyluk.ouroom_server.controllers;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import pl.mwasyluk.ouroom_server.domain.member.MemberPrivilege;
import pl.mwasyluk.ouroom_server.dto.member.MemberPresentableView;
import pl.mwasyluk.ouroom_server.dto.member.MembersForm;
import pl.mwasyluk.ouroom_server.services.member.MemberService;

@Tag(name = "Member API")
@RequiredArgsConstructor

@RestController
@RequestMapping("${server.api.prefix}/members")
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "Get all member presentables in membership")
    @GetMapping
    public ResponseEntity<Collection<MemberPresentableView>> readAllByMembershipId(
            @RequestParam UUID membershipId
    ) {
        return ResponseEntity.ok(memberService.readAllInMembership(membershipId));
    }

    @Operation(summary = "Create all members in membership")
    @PostMapping
    public ResponseEntity<Collection<MemberPresentableView>> createAll(
            @RequestParam UUID membershipId,
            @RequestBody Map<UUID, EnumSet<MemberPrivilege>> members
    ) {
        MembersForm form = new MembersForm();
        form.setMembershipId(membershipId);
        form.setMembers(members);

        return ResponseEntity.ok(memberService.createAll(form));
    }

    @Operation(summary = "Update all members in membership")
    @PatchMapping
    public ResponseEntity<Collection<MemberPresentableView>> updateAll(
            @RequestParam UUID membershipId,
            @RequestBody Map<UUID, EnumSet<MemberPrivilege>> members
    ) {
        MembersForm form = new MembersForm();
        form.setMembershipId(membershipId);
        form.setMembers(members);

        return ResponseEntity.ok(memberService.updateAll(form));
    }

    @Operation(summary = "Delete all members in membership")
    @DeleteMapping
    public ResponseEntity<?> deleteAll(
            @RequestParam UUID membershipId,
            @RequestParam List<UUID> memberIdList
    ) {
        MembersForm form = new MembersForm();
        form.setMembershipId(membershipId);
        Map<UUID, EnumSet<MemberPrivilege>> idsMap = memberIdList.stream()
                .collect(Collectors.toMap(id -> id, id -> EnumSet.noneOf(MemberPrivilege.class)));
        form.setMembers(idsMap);

        memberService.delete(form);
        return ResponseEntity.ok().build();
    }
}
