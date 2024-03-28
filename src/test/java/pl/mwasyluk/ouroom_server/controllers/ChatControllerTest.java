package pl.mwasyluk.ouroom_server.controllers;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import pl.mwasyluk.ouroom_server.domain.container.Chat;
import pl.mwasyluk.ouroom_server.domain.user.User;
import pl.mwasyluk.ouroom_server.dto.chat.ChatDetailsView;
import pl.mwasyluk.ouroom_server.dto.chat.ChatForm;
import pl.mwasyluk.ouroom_server.dto.chat.ChatPresentableView;
import pl.mwasyluk.ouroom_server.exceptions.ServiceException;
import pl.mwasyluk.ouroom_server.services.chat.ChatService;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ChatController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ChatControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ChatService chatService;

    private String baseEndpoint;
    private Chat mockChat;
    private MockMultipartFile mockFile;

    @Value("${server.api.prefix}")
    public void setBaseEndpoint(String apiPrefix) {
        this.baseEndpoint = apiPrefix + "/chats";
        this.mockChat = new Chat(new User("mock", "mock"));
        this.mockFile = new MockMultipartFile("file", "test".getBytes());
    }

    @Nested
    @DisplayName("GET /api/chats")
    class ReadAllWithPrincipalMethodTest {
        @Test
        @DisplayName("returns empty list when no chats are available")
        void returnsEmptyListWhenNoChatsAreAvailable() throws Exception {
            when(chatService.readAllWithPrincipal()).thenReturn(Collections.emptyList());

            mockMvc.perform(get(baseEndpoint))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(chatService).readAllWithPrincipal();
        }

        @Test
        @DisplayName("returns list of chats when one available")
        void returnsListOfChatsWhenOneAvailable() throws Exception {
            when(chatService.readAllWithPrincipal())
                    .thenReturn(Collections.singletonList(new ChatPresentableView(mockChat)));

            mockMvc.perform(get(baseEndpoint))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(mockChat.getId().toString()))
                    .andExpect(jsonPath("$[0].name").value(mockChat.getName()));

            verify(chatService).readAllWithPrincipal();
        }

        @Test
        @DisplayName("returns list of chats when multiple available")
        void returnsListOfChatsWhenMultipleAvailable() throws Exception {
            when(chatService.readAllWithPrincipal())
                    .thenReturn(Collections.nCopies(3, new ChatPresentableView(mockChat)));

            mockMvc.perform(get(baseEndpoint))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(3)));

            verify(chatService).readAllWithPrincipal();
        }

        @Test
        @DisplayName("returns error status code when thrown by service")
        void returnsErrorStatusCodeWhenThrownByService() throws Exception {
            when(chatService.readAllWithPrincipal())
                    .thenThrow(new ServiceException(HttpStatus.NOT_FOUND, ""));

            mockMvc.perform(get(baseEndpoint))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/chats/details")
    class ReadDetailsMethodTest {
        @Test
        @DisplayName("returns BAD_REQUEST when chatId parameter is not provided")
        void returnsBadRequestWhenChatIdParameterIsNotProvided() throws Exception {
            mockMvc.perform(get(baseEndpoint + "/details"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when chatId parameter is not valid")
        void returnsBadRequestWhenChatIdParameterIsNotValid() throws Exception {
            mockMvc.perform(get(baseEndpoint + "/details")
                            .param("chatId", "0cc43flm-f97b-4bc4-8258-1d7959a77605"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns error status code when thrown by service")
        void returnsErrorStatusCodeWhenThrownByService() throws Exception {
            when(chatService.read(mockChat.getId()))
                    .thenThrow(new ServiceException(HttpStatus.NOT_FOUND, ""));

            mockMvc.perform(get(baseEndpoint + "/details")
                            .param("chatId", mockChat.getId().toString()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns chat details when chatId parameter is valid")
        void returnsChatDetailsWhenChatIdParameterIsValid() throws Exception {
            when(chatService.read(mockChat.getId()))
                    .thenReturn(new ChatDetailsView(mockChat));

            mockMvc.perform(get(baseEndpoint + "/details")
                            .param("chatId", mockChat.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("id").value(mockChat.getId().toString()))
                    .andExpect(jsonPath("name").value(mockChat.getName()));

            verify(chatService).read(mockChat.getId());
        }
    }

    @Nested
    @DisplayName("POST /api/chats")
    class CreateMethodTest {
        @Test
        @DisplayName("does not require any parameters")
        void doesNotRequireAnyParameters() throws Exception {
            mockMvc.perform(post(baseEndpoint)
                            .contentType("multipart/form-data"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("correctly creates form when all required parameters are provided")
        void correctlyCreatesFormWhenAllParametersAreProvided() throws Exception {
            when(chatService.create(any()))
                    .thenReturn(new ChatPresentableView(mockChat));
            String testFileName = "Test";
            byte[] testFileBytes = "test".getBytes();

            mockMvc.perform(multipart(baseEndpoint)
                            .file("file", testFileBytes)
                            .param("name", testFileName)
                    )
                    .andExpect(status().isOk());

            ArgumentCaptor<ChatForm> captor = ArgumentCaptor.forClass(ChatForm.class);
            verify(chatService).create(captor.capture());
            assertEquals(testFileName, captor.getValue().getName());
            assertArrayEquals(testFileBytes, captor.getValue().getFile().getBytes());
        }

        @Test
        @DisplayName("returns error status code when thrown by service")
        void returnsErrorStatusCodeWhenThrownByService() throws Exception {
            when(chatService.create(any()))
                    .thenThrow(new ServiceException(HttpStatus.BAD_REQUEST, ""));

            mockMvc.perform(post(baseEndpoint)
                            .contentType("multipart/form-data"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns OK and presentable when service succeeds")
        void returnsOkWhenServiceReturnsPresentable() throws Exception {
            when(chatService.create(any(ChatForm.class)))
                    .thenReturn(new ChatPresentableView(mockChat));

            mockMvc.perform(post(baseEndpoint)
                            .param("name", "test")
                            .contentType("multipart/form-data"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("id").value(mockChat.getId().toString()));
        }
    }

    @Nested
    @DisplayName("PATCH /api/chats")
    class UpdateMethodTest {
        @Test
        @DisplayName("returns BAD_REQUEST when chatId parameter is not provided")
        void doesNotRequireAnyParameters() throws Exception {
            mockMvc.perform(multipart(HttpMethod.PATCH, baseEndpoint)
                            .file(mockFile)
                            .param("name", "Test")
                            .param("clearImage", "true"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when chatId parameter is not valid")
        void returnsBadRequestWhenChatIdParameterIsNotValid() throws Exception {
            mockMvc.perform(multipart(HttpMethod.PATCH, baseEndpoint)
                            .file(mockFile)
                            .param("chatId", "0cc43flm-f97b-4bc4-8258-1d7959a77605")
                            .param("name", "Test")
                            .param("clearImage", "true"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("correctly creates form when all parameters are provided")
        void correctlyCreatesFormWhenAllParametersAreProvided() throws Exception {
            mockMvc.perform(multipart(HttpMethod.PATCH, baseEndpoint)
                            .file(mockFile)
                            .param("chatId", mockChat.getId().toString())
                            .param("name", "Test")
                            .param("clearImage", "true")
                    )
                    .andExpect(status().isOk());

            ArgumentCaptor<ChatForm> captor = ArgumentCaptor.forClass(ChatForm.class);
            verify(chatService).update(captor.capture());
            assertEquals(mockChat.getId(), captor.getValue().getChatId());
            assertEquals("Test", captor.getValue().getName());
            assertArrayEquals(mockFile.getBytes(), captor.getValue().getFile().getBytes());
        }

        @Test
        @DisplayName("returns error status code when thrown by service")
        void returnsErrorStatusCodeWhenThrownByService() throws Exception {
            when(chatService.update(any()))
                    .thenThrow(new ServiceException(HttpStatus.BAD_REQUEST, ""));

            mockMvc.perform(multipart(HttpMethod.PATCH, baseEndpoint)
                            .param("chatId", mockChat.getId().toString()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns OK and presentable when service succeeds")
        void returnsOkWhenServiceReturnsPresentable() throws Exception {
            when(chatService.update(any(ChatForm.class)))
                    .thenReturn(new ChatPresentableView(mockChat));

            mockMvc.perform(multipart(HttpMethod.PATCH, baseEndpoint)
                            .param("chatId", mockChat.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("id").value(mockChat.getId().toString()));
        }
    }

    @Nested
    @DisplayName("DELETE /api/chats")
    class DeleteMethodTest {
        @Test
        @DisplayName("returns BAD_REQUEST when chatId parameter is not provided")
        void returnsBadRequestWhenChatIdParameterIsNotProvided() throws Exception {
            mockMvc.perform(delete(baseEndpoint))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when chatId parameter is not valid")
        void returnsBadRequestWhenChatIdParameterIsNotValid() throws Exception {
            mockMvc.perform(delete(baseEndpoint)
                            .param("chatId", "0cc43flm-f97b-4bc4-8258-1d7959a77605"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns error status code when thrown by service")
        void returnsErrorStatusCodeWhenThrownByService() throws Exception {
            doThrow(new ServiceException(HttpStatus.NOT_FOUND, ""))
                    .when(chatService).delete(mockChat.getId());

            mockMvc.perform(delete(baseEndpoint)
                            .param("chatId", mockChat.getId().toString()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns OK when service succeeds")
        void returnsOkWhenServiceSucceeds() throws Exception {
            doNothing().when(chatService).delete(mockChat.getId());

            mockMvc.perform(delete(baseEndpoint)
                            .param("chatId", mockChat.getId().toString()))
                    .andExpect(status().isOk());
        }
    }
}
