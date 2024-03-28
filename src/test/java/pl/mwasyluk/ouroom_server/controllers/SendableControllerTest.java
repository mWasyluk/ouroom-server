package pl.mwasyluk.ouroom_server.controllers;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import pl.mwasyluk.ouroom_server.domain.container.Chat;
import pl.mwasyluk.ouroom_server.domain.media.Media;
import pl.mwasyluk.ouroom_server.domain.media.source.DataSource;
import pl.mwasyluk.ouroom_server.domain.sendable.ChatSendable;
import pl.mwasyluk.ouroom_server.domain.sendable.Sendable;
import pl.mwasyluk.ouroom_server.domain.user.User;
import pl.mwasyluk.ouroom_server.dto.sendable.SendableForm;
import pl.mwasyluk.ouroom_server.dto.sendable.SendableView;
import pl.mwasyluk.ouroom_server.services.media.MediaService;
import pl.mwasyluk.ouroom_server.services.sendable.SendableService;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.mwasyluk.ouroom_server.domain.media.source.DataSourceTestUtil.JPEG_BYTES;

@WebMvcTest(value = SendableController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class SendableControllerTest {

    private final String mockInternalUrlEndpoint = "http://localhost:8080/api/media";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SendableService sendableService;
    @MockBean
    private MediaService mediaService;

    private String baseEndpoint;
    private Chat mockChat;
    private Sendable mockSendable;
    private MockMultipartFile mockFile;
    private Media mockMedia;

    @Value("${server.api.prefix}")
    public void setBaseEndpoint(String apiPrefix) {
        this.baseEndpoint = apiPrefix + "/sendables";
    }

    @BeforeEach
    void setUp() {
        User mockUser = new User("mock", "mock");
        this.mockChat = new Chat(mockUser);
        this.mockSendable = new ChatSendable(mockUser, "mock");
        this.mockSendable.setContainer(mockChat);
        this.mockFile = new MockMultipartFile("file", "test".getBytes());
        this.mockMedia = new Media(DataSource.of(JPEG_BYTES)) {
            @Override
            protected void validate() {
            }

            @Override
            public @NonNull String getInternalUrl() {
                return mockInternalUrlEndpoint + "/" + getId();
            }
        };
    }

    @Nested
    @DisplayName("GET /api/sendables")
    class ReadAllByContainerIdMethodTest {
        @Test
        @DisplayName("returns BAD_REQUEST when containerId parameter is not provided")
        void returnsBadRequestWhenContainerIdParameterIsNotProvided() throws Exception {
            mockMvc.perform(get(baseEndpoint))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when containerId parameter is not valid")
        void returnsBadRequestWhenContainerIdParameterIsNotValid() throws Exception {
            mockMvc.perform(get(baseEndpoint)
                            .param("containerId", "0cc43flm-f97b-4bc4-8258-1d7959a77605"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns empty list when no sendables are available")
        void returnsEmptyListWhenNoSendablesAreAvailable() throws Exception {
            when(sendableService.readAllFromContainer(mockChat.getId()))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get(baseEndpoint)
                            .param("containerId", mockChat.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("returns list of sendables when one available")
        void returnsListOfSendablesWhenOneAvailable() throws Exception {
            when(sendableService.readAllFromContainer(mockChat.getId()))
                    .thenReturn(Collections.singletonList(new SendableView(mockSendable)));

            mockMvc.perform(get(baseEndpoint)
                            .param("containerId", mockChat.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        @DisplayName("returns list of sendables when multiple available")
        void returnsListOfSendablesWhenMultipleAvailable() throws Exception {
            when(sendableService.readAllFromContainer(mockChat.getId()))
                    .thenReturn(List.of(new SendableView(mockSendable), new SendableView(mockSendable)));

            mockMvc.perform(get(baseEndpoint)
                            .param("containerId", mockChat.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)));
        }
    }

    @Nested
    @DisplayName("POST /api/sendables")
    class CreateMethodTest {
        @Test
        @DisplayName("returns BAD_REQUEST when containerId parameter is not provided")
        void returnsBadRequestWhenContainerIdParameterIsNotProvided() throws Exception {
            mockMvc.perform(multipart(baseEndpoint)
                            .file(mockFile)
                            .param("message", "mock"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when containerId parameter is not valid")
        void returnsBadRequestWhenContainerIdParameterIsNotValid() throws Exception {
            mockMvc.perform(multipart(baseEndpoint)
                            .file(mockFile)
                            .param("containerId", "0cc43flm-f97b-4bc4-8258-1d7959a77605")
                            .param("message", "mock"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when message parameter is not provided")
        void returnsBadRequestWhenMessageParameterIsNotProvided() throws Exception {
            mockMvc.perform(post(baseEndpoint)
                            .param("containerId", mockChat.getId().toString())
                            .contentType("multipart/form-data"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns OK when containerId and message parameters are valid")
        void returnsOkWhenContainerIdAndMessageParametersAreValid() throws Exception {
            mockMvc.perform(post(baseEndpoint)
                            .param("containerId", mockChat.getId().toString())
                            .param("message", "mock")
                            .contentType("multipart/form-data"))
                    .andExpect(status().isOk());

            verify(sendableService).create(any());
        }

        @Test
        @DisplayName("correctly appends media internal url to message when file is provided")
        void correctlyAppendsMediaInternalUrlToMessageWhenFileIsProvided() throws Exception {
            when(mediaService.create(any()))
                    .thenReturn(mockMedia);

            mockMvc.perform(multipart(baseEndpoint)
                            .file(mockFile)
                            .param("containerId", mockChat.getId().toString())
                            .param("message", "mock"))
                    .andExpect(status().isOk());

            ArgumentCaptor<SendableForm> formCaptor = ArgumentCaptor.forClass(SendableForm.class);
            verify(sendableService).create(formCaptor.capture());
            assertEquals("mock\n" + mockMedia.getInternalUrl(), formCaptor.getValue().getMessage());
        }

        @Test
        @DisplayName("correctly creates a form when all parameters are valid")
        void correctlyCreatesAFormWhenAllParametersAreValid() throws Exception {
            UUID containerId = mockChat.getId();
            String message = "mock";
            when(mediaService.create(any()))
                    .thenReturn(mockMedia);

            mockMvc.perform(multipart(baseEndpoint)
                            .file(mockFile)
                            .param("containerId", containerId.toString())
                            .param("message", message))
                    .andExpect(status().isOk());

            ArgumentCaptor<SendableForm> formCaptor = ArgumentCaptor.forClass(SendableForm.class);
            verify(sendableService).create(formCaptor.capture());
            assertAll(() -> {
                assertEquals(containerId, formCaptor.getValue().getContainerId());
                assertTrue(formCaptor.getValue().getMessage().startsWith(message));
            });
        }

        @Test
        @DisplayName("does not append anything to message when file is not provided")
        void doesNotAppendAnythingToMessageWhenFileIsNotProvided() throws Exception {
            mockMvc.perform(post(baseEndpoint)
                            .param("containerId", mockChat.getId().toString())
                            .param("message", "mock")
                            .contentType("multipart/form-data"))
                    .andExpect(status().isOk());

            ArgumentCaptor<SendableForm> formCaptor = ArgumentCaptor.forClass(SendableForm.class);
            verify(sendableService).create(formCaptor.capture());
            assertEquals("mock", formCaptor.getValue().getMessage());
        }
    }

    @Nested
    @DisplayName("PATCH /api/sendables")
    class UpdateMethodTest {
        @Test
        @DisplayName("returns BAD_REQUEST when sendableId parameter is not provided")
        void returnsBadRequestWhenSendableIdParameterIsNotProvided() throws Exception {
            mockMvc.perform(patch(baseEndpoint)
                            .param("message", "mock"))
                    .andExpect(status().isBadRequest());

            verify(sendableService, never()).update(any());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when sendableId parameter is not valid")
        void returnsBadRequestWhenSendableIdParameterIsNotValid() throws Exception {
            mockMvc.perform(patch(baseEndpoint)
                            .param("sendableId", "0cc43flm-f97b-4bc4-8258-1d7959a77605")
                            .param("message", "mock"))
                    .andExpect(status().isBadRequest());

            verify(sendableService, never()).update(any());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when message parameter is not provided")
        void returnsBadRequestWhenMessageParameterIsNotProvided() throws Exception {
            mockMvc.perform(patch(baseEndpoint)
                            .param("sendableId", mockSendable.getId().toString()))
                    .andExpect(status().isBadRequest());

            verify(sendableService, never()).update(any());
        }

        @Test
        @DisplayName("returns OK when sendableId and message parameters are valid")
        void returnsOkWhenSendableIdAndMessageParametersAreValid() throws Exception {
            mockMvc.perform(patch(baseEndpoint)
                            .param("sendableId", mockSendable.getId().toString())
                            .param("message", "mock"))
                    .andExpect(status().isOk());

            verify(sendableService).update(any());
        }

        @Test
        @DisplayName("correctly creates a form when all parameters are valid")
        void correctlyCreatesAFormWhenAllParametersAreValid() throws Exception {
            UUID sendableId = mockSendable.getId();
            String message = "mock";

            mockMvc.perform(patch(baseEndpoint)
                            .param("sendableId", sendableId.toString())
                            .param("message", message))
                    .andExpect(status().isOk());

            ArgumentCaptor<SendableForm> formCaptor = ArgumentCaptor.forClass(SendableForm.class);
            verify(sendableService).update(formCaptor.capture());
            assertAll(() -> {
                assertEquals(sendableId, formCaptor.getValue().getSendableId());
                assertEquals(message, formCaptor.getValue().getMessage());
            });
        }

        @Test
        @DisplayName("returns sendable view when returned from service")
        void returnsSendableViewWhenReturnedFromService() throws Exception {
            when(sendableService.update(any()))
                    .thenReturn(new SendableView(mockSendable));

            mockMvc.perform(patch(baseEndpoint)
                            .param("sendableId", mockSendable.getId().toString())
                            .param("message", "mock"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("id").value(mockSendable.getId().toString()))
                    .andExpect(jsonPath("containerId").value(mockSendable.getContainer().getId().toString()));
        }
    }

    @Nested
    @DisplayName("DELETE /api/sendables")
    class DeleteMethodTest {
        @Test
        @DisplayName("returns BAD_REQUEST when sendableId parameter is not provided")
        void returnsBadRequestWhenSendableIdParameterIsNotProvided() throws Exception {
            mockMvc.perform(delete(baseEndpoint))
                    .andExpect(status().isBadRequest());

            verify(sendableService, never()).delete(any());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when sendableId parameter is not valid")
        void returnsBadRequestWhenSendableIdParameterIsNotValid() throws Exception {
            mockMvc.perform(delete(baseEndpoint)
                            .param("sendableId", "0cc43flm-f97b-4bc4-8258-1d7959a77605"))
                    .andExpect(status().isBadRequest());

            verify(sendableService, never()).delete(any());
        }

        @Test
        @DisplayName("returns OK when sendableId parameter is valid")
        void returnsOkWhenSendableIdParameterIsValid() throws Exception {
            mockMvc.perform(delete(baseEndpoint)
                            .param("sendableId", mockSendable.getId().toString()))
                    .andExpect(status().isOk());

            verify(sendableService).delete(mockSendable.getId());
        }
    }
}
