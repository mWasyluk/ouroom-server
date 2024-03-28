package pl.mwasyluk.ouroom_server.controllers;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import pl.mwasyluk.ouroom_server.domain.container.Chat;
import pl.mwasyluk.ouroom_server.domain.member.ChatMember;
import pl.mwasyluk.ouroom_server.domain.member.Member;
import pl.mwasyluk.ouroom_server.domain.user.User;
import pl.mwasyluk.ouroom_server.dto.member.MemberPresentableView;
import pl.mwasyluk.ouroom_server.dto.member.MembersForm;
import pl.mwasyluk.ouroom_server.services.member.MemberService;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.mwasyluk.ouroom_server.domain.member.MemberPrivilege.ADD_MESSAGES;
import static pl.mwasyluk.ouroom_server.domain.member.MemberPrivilege.DELETE_MESSAGES;

@WebMvcTest(value = MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private MemberService memberService;

    private String baseEndpoint;
    private Chat mockChat;
    private Member mockMember;

    @Value("${server.api.prefix}")
    public void setBaseEndpoint(String apiPrefix) {
        this.baseEndpoint = apiPrefix + "/members";
        this.mockChat = new Chat(new User("mock", "mock"));
        this.mockMember = new ChatMember(new User("mock", "mock"), mockChat, Collections.emptySet());
    }

    @Nested
    @DisplayName("GET /api/members")
    class ReadAllByMembershipIdMethodTest {
        @Test
        @DisplayName("returns BAD_REQUEST when membershipId parameter is not provided")
        void returnsBadRequestWhenMembershipIdParameterIsNotProvided() throws Exception {
            mockMvc.perform(get(baseEndpoint))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when membershipId parameter is not valid")
        void returnsBadRequestWhenMembershipIdParameterIsNotValid() throws Exception {
            mockMvc.perform(get(baseEndpoint)
                            .param("membershipId", "0cc43flm-f97b-4bc4-8258-1d7959a77605"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns empty list when no members are available")
        void returnsEmptyListWhenNoMembersAreAvailable() throws Exception {
            when(memberService.readAllInMembership(mockChat.getId()))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get(baseEndpoint)
                            .param("membershipId", mockChat.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("returns list of members when one available")
        void returnsListOfMembersWhenOneAvailable() throws Exception {
            when(memberService.readAllInMembership(mockChat.getId()))
                    .thenReturn(Collections.singletonList(new MemberPresentableView(mockMember)));

            mockMvc.perform(get(baseEndpoint)
                            .param("membershipId", mockChat.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        @DisplayName("returns list of members when multiple available")
        void returnsListOfMembersWhenMultipleAvailable() throws Exception {
            when(memberService.readAllInMembership(mockChat.getId()))
                    .thenReturn(List.of(new MemberPresentableView(mockMember), new MemberPresentableView(mockMember)));

            mockMvc.perform(get(baseEndpoint)
                            .param("membershipId", mockChat.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)));
        }
    }

    @Nested
    @DisplayName("POST /api/members")
    class CreateAllMethodTest {
        @Test
        @DisplayName("returns BAD_REQUEST when membershipId parameter is not provided")
        void returnsBadRequestWhenMembershipIdParameterIsNotProvided() throws Exception {
            mockMvc.perform(post(baseEndpoint))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when membershipId parameter is not valid")
        void returnsBadRequestWhenMembershipIdParameterIsNotValid() throws Exception {
            mockMvc.perform(post(baseEndpoint)
                            .param("membershipId", "0cc43flm-f97b-4bc4-8258-1d7959a77605"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when members parameter is not provided")
        void returnsBadRequestWhenMembersParameterIsNotProvided() throws Exception {
            mockMvc.perform(post(baseEndpoint)
                            .param("membershipId", mockChat.getId().toString()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when members parameter is not valid")
        void returnsBadRequestWhenMembersParameterIsNotValid() throws Exception {
            mockMvc.perform(post(baseEndpoint)
                            .param("membershipId", mockChat.getId().toString())
                            .content("[]")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when members parameter contains invalid uuid")
        void returnsBadRequestWhenMembersParameterContainsInvalidUuid() throws Exception {
            mockMvc.perform(post(baseEndpoint)
                            .param("membershipId", mockChat.getId().toString())
                            .content("{\"0cc43flm-f97b-4bc4-8258-1d7959a77605\": []}")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when members parameter contains invalid privilege")
        void returnsBadRequestWhenMembersParameterContainsInvalidPrivilege() throws Exception {
            mockMvc.perform(post(baseEndpoint)
                            .param("membershipId", mockChat.getId().toString())
                            .content("{\"" + UUID.randomUUID() + "\": [\"INVALID_PRIVILEGE\"]}")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns OK when membershipId and members parameters are valid")
        void returnsOkWhenMembershipIdAndMembersParametersAreValid() throws Exception {
            mockMvc.perform(post(baseEndpoint)
                            .param("membershipId", mockChat.getId().toString())
                            .content("{\"" + UUID.randomUUID() + "\": [" + "\"ADD_MESSAGES\"" + "]}")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("correctly creates a form when all parameters are valid")
        void correctlyCreatesAFormWhenAllParametersAreValid() throws Exception {
            UUID membershipId = mockChat.getId();
            UUID u1Id = UUID.randomUUID();
            UUID u2Id = UUID.randomUUID();
            mockMvc.perform(post(baseEndpoint)
                            .param("membershipId", membershipId.toString())
                            .content("{\"" + u1Id + "\": [\"" + ADD_MESSAGES + "\"], "
                                     + "\"" + u2Id + "\": [\"" + ADD_MESSAGES + "\", "
                                     + "\"" + DELETE_MESSAGES + "\"]}")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            ArgumentCaptor<MembersForm> formCaptor = ArgumentCaptor.forClass(MembersForm.class);
            verify(memberService).createAll(formCaptor.capture());

            assertAll(() -> {
                assertEquals(membershipId, formCaptor.getValue().getMembershipId());
                assertEquals(2, formCaptor.getValue().getMembers().size());
                assertEquals(Collections.singleton(ADD_MESSAGES), formCaptor.getValue().getMembers().get(u1Id));
                assertEquals(Set.of(ADD_MESSAGES, DELETE_MESSAGES), formCaptor.getValue().getMembers().get(u2Id));
            });
        }

        @Test
        @DisplayName("returns list of members when one available")
        void returnsListOfMembersWhenOneAvailable() throws Exception {
            when(memberService.createAll(any()))
                    .thenReturn(Collections.singletonList(new MemberPresentableView(mockMember)));

            mockMvc.perform(post(baseEndpoint)
                            .param("membershipId", mockChat.getId().toString())
                            .content("{\"" + UUID.randomUUID() + "\": [\"" + ADD_MESSAGES + "\"]}")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        @DisplayName("returns list of members when multiple available")
        void returnsListOfMembersWhenMultipleAvailable() throws Exception {
            when(memberService.createAll(any()))
                    .thenReturn(List.of(new MemberPresentableView(mockMember), new MemberPresentableView(mockMember)));

            mockMvc.perform(post(baseEndpoint)
                            .param("membershipId", mockChat.getId().toString())
                            .content("{\"" + UUID.randomUUID() + "\": [\"" + ADD_MESSAGES + "\"]}")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)));
        }
    }

    @Nested
    @DisplayName("PATCH /api/members")
    class UpdateAllMethodTest {
        @Test
        @DisplayName("returns BAD_REQUEST when membershipId parameter is not provided")
        void returnsBadRequestWhenMembershipIdParameterIsNotProvided() throws Exception {
            mockMvc.perform(patch(baseEndpoint))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when membershipId parameter is not valid")
        void returnsBadRequestWhenMembershipIdParameterIsNotValid() throws Exception {
            mockMvc.perform(patch(baseEndpoint)
                            .param("membershipId", "0cc43flm-f97b-4bc4-8258-1d7959a77605")
                            .content("{\"" + UUID.randomUUID() + "\": [\" " + ADD_MESSAGES + "\"]}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when members parameter is not provided")
        void returnsBadRequestWhenMembersParameterIsNotProvided() throws Exception {
            mockMvc.perform(patch(baseEndpoint)
                            .param("membershipId", mockChat.getId().toString()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when members parameter is not valid")
        void returnsBadRequestWhenMembersParameterIsNotValid() throws Exception {
            mockMvc.perform(patch(baseEndpoint)
                            .param("membershipId", mockChat.getId().toString())
                            .content("[]")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when members parameter contains invalid uuid")
        void returnsBadRequestWhenMembersParameterContainsInvalidUuid() throws Exception {
            mockMvc.perform(patch(baseEndpoint)
                            .param("membershipId", mockChat.getId().toString())
                            .content("{\"0cc43flm-f97b-4bc4-8258-1d7959a77605\": [\"" + ADD_MESSAGES + "\"]}")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when members parameter contains invalid privilege")
        void returnsBadRequestWhenMembersParameterContainsInvalidPrivilege() throws Exception {
            mockMvc.perform(patch(baseEndpoint)
                            .param("membershipId", mockChat.getId().toString())
                            .content("{\"" + UUID.randomUUID() + "\": [\"INVALID_PRIVILEGE\"]}")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns OK when when member without privileges is provided")
        void returnsOkWhenMemberWithoutPrivilegesIsProvided() throws Exception {
            mockMvc.perform(patch(baseEndpoint)
                            .param("membershipId", mockChat.getId().toString())
                            .content("{\"" + UUID.randomUUID() + "\": []}")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("returns OK when membershipId and members parameters are valid")
        void returnsOkWhenMembershipIdAndMembersParametersAreValid() throws Exception {
            mockMvc.perform(patch(baseEndpoint)
                            .param("membershipId", mockChat.getId().toString())
                            .content("{\"" + UUID.randomUUID() + "\": [\"" + ADD_MESSAGES + "\"]}")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("correctly creates a form when all parameters are valid")
        void correctlyCreatesAFormWhenAllParametersAreValid() throws Exception {
            UUID membershipId = mockChat.getId();
            UUID u1Id = UUID.randomUUID();
            UUID u2Id = UUID.randomUUID();
            mockMvc.perform(patch(baseEndpoint)
                            .param("membershipId", membershipId.toString())
                            .content("{\"" + u1Id + "\": [\"" + ADD_MESSAGES + "\"], "
                                     + "\"" + u2Id + "\": [\"" + ADD_MESSAGES + "\", "
                                     + "\"" + DELETE_MESSAGES + "\"]}")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            ArgumentCaptor<MembersForm> formCaptor = ArgumentCaptor.forClass(MembersForm.class);
            verify(memberService).updateAll(formCaptor.capture());
            assertAll(() -> {
                assertEquals(membershipId, formCaptor.getValue().getMembershipId());
                assertEquals(2, formCaptor.getValue().getMembers().size());
                assertEquals(Collections.singleton(ADD_MESSAGES), formCaptor.getValue().getMembers().get(u1Id));
                assertEquals(Set.of(ADD_MESSAGES, DELETE_MESSAGES), formCaptor.getValue().getMembers().get(u2Id));
            });
        }

        @Test
        @DisplayName("returns list of members when updated")
        void returnsListOfMembersWhenUpdated() throws Exception {
            when(memberService.updateAll(any()))
                    .thenReturn(List.of(new MemberPresentableView(mockMember), new MemberPresentableView(mockMember)));

            mockMvc.perform(patch(baseEndpoint)
                            .param("membershipId", mockChat.getId().toString())
                            .content("{\"" + UUID.randomUUID() + "\": [\"" + ADD_MESSAGES + "\"]}")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)));
        }
    }

    @Nested
    @DisplayName("DELETE /api/members")
    class DeleteAllMethodTest {
        @Test
        @DisplayName("returns BAD_REQUEST when membershipId parameter is not provided")
        void returnsBadRequestWhenMembershipIdParameterIsNotProvided() throws Exception {
            mockMvc.perform(delete(baseEndpoint))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when membershipId parameter is not valid")
        void returnsBadRequestWhenMembershipIdParameterIsNotValid() throws Exception {
            mockMvc.perform(delete(baseEndpoint)
                            .param("membershipId", "0cc43flm-f97b-4bc4-8258-1d7959a77605")
                            .content("[]")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when memberIdList parameter is not provided")
        void returnsBadRequestWhenMemberIdListParameterIsNotProvided() throws Exception {
            mockMvc.perform(delete(baseEndpoint)
                            .param("membershipId", mockChat.getId().toString()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when memberIdList parameter is not valid")
        void returnsBadRequestWhenMemberIdListParameterIsNotValid() throws Exception {
            mockMvc.perform(delete(baseEndpoint)
                            .param("membershipId", mockChat.getId().toString())
                            .content("{}")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns OK when membershipId and memberIdList parameters are valid")
        void returnsOkWhenMembershipIdAndMemberIdListParametersAreValid() throws Exception {
            mockMvc.perform(delete(baseEndpoint)
                            .param("membershipId", mockChat.getId().toString())
                            .param("memberIdList", UUID.randomUUID().toString()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("correctly creates a form when all parameters are valid")
        void correctlyCreatesAFormWhenAllParametersAreValid() throws Exception {
            UUID chatId = mockChat.getId();
            mockMvc.perform(delete(baseEndpoint)
                            .param("membershipId", chatId.toString())
                            .param("memberIdList", UUID.randomUUID().toString())
                            .param("memberIdList", UUID.randomUUID().toString())
                            .param("memberIdList", UUID.randomUUID().toString()))
                    .andExpect(status().isOk());

            ArgumentCaptor<MembersForm> formCaptor = ArgumentCaptor.forClass(MembersForm.class);
            verify(memberService).delete(formCaptor.capture());
            assertAll(() -> {
                assertEquals(chatId, formCaptor.getValue().getMembershipId());
                assertEquals(3, formCaptor.getValue().getMembers().size());
                formCaptor.getValue().getMembers().keySet().forEach(Assertions::assertNotNull);
            });
        }
    }
}
