package pl.mwasyluk.ouroom_server.controllers;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import pl.mwasyluk.ouroom_server.domain.user.User;
import pl.mwasyluk.ouroom_server.domain.user.UserAuthority;
import pl.mwasyluk.ouroom_server.dto.user.UserDetailsForm;
import pl.mwasyluk.ouroom_server.dto.user.UserDetailsView;
import pl.mwasyluk.ouroom_server.dto.user.UserPresentableForm;
import pl.mwasyluk.ouroom_server.dto.user.UserPresentableView;
import pl.mwasyluk.ouroom_server.exceptions.ServiceException;
import pl.mwasyluk.ouroom_server.services.user.UserService;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
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

@WebMvcTest(value = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserControllerTest {
    private String baseEndpoint;
    private User mockUser;

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;

    @Value("${server.api.prefix}")
    public void setApiPrefix(String apiPrefix) {
        this.baseEndpoint = apiPrefix + "/users";
        this.mockUser = new User("mock", "mock", Set.of(UserAuthority.USER));
    }

    @Nested
    @DisplayName("GET /api/users")
    class GetPresentableByUserIdMethodTest {
        @Test
        @DisplayName("passes null when no parameters are provided")
        void passesNullWhenNoParametersAreProvided() throws Exception {
            mockMvc.perform(get(baseEndpoint))
                    .andExpect(status().isOk());

            verify(userService).readPresentable(null);
        }

        @Test
        @DisplayName("passes id when userId parameter is provided")
        void passesIdWhenUserIdParameterIsProvided() throws Exception {
            UUID userId = UUID.randomUUID();
            mockMvc.perform(get(baseEndpoint)
                    .param("userId", userId.toString()));

            verify(userService).readPresentable(userId);
        }

        @Test
        @DisplayName("returns error status code when thrown by service")
        void returnsErrorStatusCodeWhenThrownByService() throws Exception {
            when(userService.readPresentable(any()))
                    .thenThrow(new ServiceException(HttpStatus.NOT_FOUND, ""));

            mockMvc.perform(get(baseEndpoint))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns OK and presentable when service succeeds")
        void returnsOkWhenServiceReturnsPresentable() throws Exception {
            when(userService.readPresentable(any(UUID.class)))
                    .thenReturn(new UserPresentableView(UUID.randomUUID(), "Test Test", null, null));

            mockMvc.perform(get(baseEndpoint)
                            .param("userId", UUID.randomUUID().toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("id").isNotEmpty());
        }

        @Test
        @DisplayName("does not throws exception when userId is not provided")
        void doesNotThrowExceptionWhenUserIdIsNotProvided() {
            assertDoesNotThrow(() -> mockMvc.perform(get(baseEndpoint)));
        }
    }

    @Nested
    @DisplayName("POST /api/users")
    class CreateNewUserMethodTest {

        @Test
        @DisplayName("returns BAD_REQUEST when email param is not provided")
        void returnsBadRequestWhenEmailIsNotProvided() throws Exception {
            mockMvc.perform(post(baseEndpoint)
                            .param("password", "test")
                            .param("authorities", UserAuthority.USER.toString()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when password param is not provided")
        void returnsBadRequestWhenPasswordIsNotProvided() throws Exception {
            mockMvc.perform(post(baseEndpoint)
                            .param("email", "test")
                            .param("authorities", UserAuthority.USER.toString()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("correctly creates form when all required parameters are provided")
        void correctlyCreatesFormWhenAllParametersAreProvided() throws Exception {
            when(userService.create(any()))
                    .thenReturn(new UserDetailsView(mockUser));

            mockMvc.perform(post(baseEndpoint)
                            .param("email", "test")
                            .param("password", "test"))
                    .andExpect(status().isOk());

            ArgumentCaptor<UserDetailsForm> captor = ArgumentCaptor.forClass(UserDetailsForm.class);
            verify(userService).create(captor.capture());
            assertAll(() -> {
                assertEquals("test", captor.getValue().getEmail());
                assertEquals("test", captor.getValue().getPassword());
            });
        }

        @Test
        @DisplayName("passes authorities when provided")
        void passesAuthoritiesWhenProvided() throws Exception {
            when(userService.create(any()))
                    .thenReturn(new UserDetailsView(mockUser));

            mockMvc.perform(post(baseEndpoint)
                            .param("email", "test")
                            .param("password", "test")
                            .param("authorities", UserAuthority.USER.toString()))
                    .andExpect(status().isOk());

            ArgumentCaptor<UserDetailsForm> captor = ArgumentCaptor.forClass(UserDetailsForm.class);
            verify(userService).create(captor.capture());
            assertEquals(Set.of(UserAuthority.USER), captor.getValue().getAuthorities());
        }

        @Test
        @DisplayName("returns error status code when thrown by service")
        void returnsErrorStatusCodeWhenThrownByService() throws Exception {
            when(userService.create(any()))
                    .thenThrow(new ServiceException(HttpStatus.BAD_REQUEST, ""));

            mockMvc.perform(post(baseEndpoint))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns OK and presentable when service succeeds")
        void returnsOkWhenServiceReturnsPresentable() throws Exception {
            when(userService.create(any(UserDetailsForm.class)))
                    .thenReturn(new UserDetailsView(mockUser));

            mockMvc.perform(post(baseEndpoint)
                            .param("email", "test")
                            .param("password", "test"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("userId").value(mockUser.getId().toString()));
        }
    }

    @Nested
    @DisplayName("DELETE /api/users")
    class DeleteUserMethodTest {
        @Test
        @DisplayName("passes null when id or userId parameter is provided")
        void passesNullWhenIdParameterIsProvided() throws Exception {
            mockMvc.perform(delete(baseEndpoint)
                    .param("id", UUID.randomUUID().toString())
                    .param("userId", UUID.randomUUID().toString()));

            verify(userService).disable(null);
        }

        @Test
        @DisplayName("returns OK when service succeeds")
        void returnsOkWhenServiceSucceeds() throws Exception {
            mockMvc.perform(delete(baseEndpoint))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("returns error status code when thrown by service")
        void returnsErrorStatusCodeWhenThrownByService() throws Exception {
            doThrow(new ServiceException(HttpStatus.NOT_FOUND, ""))
                    .when(userService).disable(null);

            mockMvc.perform(delete(baseEndpoint))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/users/details")
    class GetPrincipalDetailsMethodTest {
        @Test
        @DisplayName("passes null when id or userId parameter is provided")
        void passesNullWhenIdParameterIsProvided() throws Exception {
            mockMvc.perform(get(baseEndpoint + "/details")
                    .param("id", UUID.randomUUID().toString())
                    .param("userId", UUID.randomUUID().toString()));

            verify(userService).readDetails(null);
        }

        @Test
        @DisplayName("returns OK and details when service succeeds")
        void returnsOkWhenServiceSucceeds() throws Exception {
            when(userService.readDetails(null))
                    .thenReturn(new UserDetailsView(mockUser));

            mockMvc.perform(get(baseEndpoint + "/details"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("userId").value(mockUser.getId().toString()));
        }

        @Test
        @DisplayName("returns error status code when thrown by service")
        void returnsErrorStatusCodeWhenThrownByService() throws Exception {
            when(userService.readDetails(null))
                    .thenThrow(new ServiceException(HttpStatus.NOT_FOUND, ""));

            mockMvc.perform(get(baseEndpoint + "/details"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/details")
    class UpdatePrincipalDetailsMethodTest {
        @Test
        @DisplayName("does not require any parameters")
        void doesNotRequireAnyParameters() throws Exception {
            mockMvc.perform(patch(baseEndpoint + "/details"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("correctly creates form when all required parameters are provided")
        void correctlyCreatesFormWhenAllParametersAreProvided() throws Exception {
            mockMvc.perform(patch(baseEndpoint + "/details")
                            .param("email", "test")
                            .param("password", "test"))
                    .andExpect(status().isOk());

            ArgumentCaptor<UserDetailsForm> captor = ArgumentCaptor.forClass(UserDetailsForm.class);
            verify(userService).updateAccount(captor.capture());
            assertAll(() -> {
                assertEquals("test", captor.getValue().getEmail());
                assertEquals("test", captor.getValue().getPassword());
            });
        }

        @Test
        @DisplayName("correctly creates form when only email or password is provided")
        void correctlyCreatesFormWhenOnlyEmailOrPasswordIsProvided() throws Exception {
            when(userService.updateAccount(any()))
                    .thenReturn(new UserDetailsView(mockUser));

            mockMvc.perform(patch(baseEndpoint + "/details")
                            .param("email", "test"))
                    .andExpect(status().isOk());
            mockMvc.perform(patch(baseEndpoint + "/details")
                            .param("password", "test"))
                    .andExpect(status().isOk());

            ArgumentCaptor<UserDetailsForm> captor = ArgumentCaptor.forClass(UserDetailsForm.class);
            verify(userService, times(2)).updateAccount(captor.capture());
            List<UserDetailsForm> allValues = captor.getAllValues();
            assertAll(() -> {
                assertEquals("test", allValues.get(0).getEmail());
                assertNull(allValues.get(0).getPassword());

                assertNull(allValues.get(1).getEmail());
                assertEquals("test", allValues.get(1).getPassword());
            });
        }

        @Test
        @DisplayName("returns OK and details when service succeeds")
        void returnsOkWhenServiceSucceeds() throws Exception {
            when(userService.updateAccount(any()))
                    .thenReturn(new UserDetailsView(mockUser));

            mockMvc.perform(patch(baseEndpoint + "/details"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("userId").value(mockUser.getId().toString()));
        }

        @Test
        @DisplayName("returns error status code when thrown by service")
        void returnsErrorStatusCodeWhenThrownByService() throws Exception {
            when(userService.updateAccount(any()))
                    .thenThrow(new ServiceException(HttpStatus.NOT_FOUND, ""));

            mockMvc.perform(patch(baseEndpoint + "/details"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/profile")
    class UpdatePrincipalProfileMethodTest {
        @Test
        @DisplayName("does not require any parameters")
        void doesNotRequireAnyParameters() throws Exception {
            mockMvc.perform(patch(baseEndpoint + "/profile")
                            .contentType("multipart/form-data"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("correctly creates form when all required parameters are provided")
        void correctlyCreatesFormWhenAllParametersAreProvided() throws Exception {
            mockMvc.perform(multipart(baseEndpoint + "/profile")
                            .file("imageFile", "test".getBytes())
                            .param("firstname", "test")
                            .param("lastname", "test")
                            .param("clearImage", "true")
                            .with(request -> {
                                request.setMethod("PATCH");
                                return request;
                            }))
                    .andExpect(status().isOk());

            ArgumentCaptor<UserPresentableForm> captor = ArgumentCaptor.forClass(UserPresentableForm.class);
            verify(userService).updateProfile(captor.capture());
            assertAll(() -> {
                assertEquals("test", captor.getValue().getFirstname());
                assertEquals("test", captor.getValue().getLastname());
                assertTrue(captor.getValue().isClearImage());
                assertArrayEquals("test".getBytes(), captor.getValue().getFile().getBytes());
            });
        }

        @Test
        @DisplayName("correctly creates form when only firstname or lastname is provided")
        void correctlyCreatesFormWhenOnlyFirstnameOrLastnameIsProvided() throws Exception {
            mockMvc.perform(patch(baseEndpoint + "/profile")
                    .param("firstname", "test")
                    .contentType("multipart/form-data"));
            mockMvc.perform(patch(baseEndpoint + "/profile")
                    .contentType("multipart/form-data")
                    .param("lastname", "test"));

            ArgumentCaptor<UserPresentableForm> captor = ArgumentCaptor.forClass(UserPresentableForm.class);
            verify(userService, times(2)).updateProfile(captor.capture());
            List<UserPresentableForm> allValues = captor.getAllValues();
            assertAll(() -> {
                assertEquals("test", allValues.get(0).getFirstname());
                assertNull(allValues.get(0).getLastname());

                assertNull(allValues.get(1).getFirstname());
                assertEquals("test", allValues.get(1).getLastname());
            });
        }

        @Test
        @DisplayName("passes false when clearImage is not provided")
        void passesFalseWhenClearImageIsNotProvided() throws Exception {
            mockMvc.perform(multipart(baseEndpoint + "/profile")
                            .file("imageFile", "test".getBytes())
                            .param("firstname", "test")
                            .param("lastname", "test")
                            .with(request -> {
                                request.setMethod("PATCH");
                                return request;
                            }))
                    .andExpect(status().isOk());

            ArgumentCaptor<UserPresentableForm> captor = ArgumentCaptor.forClass(UserPresentableForm.class);
            verify(userService).updateProfile(captor.capture());
            assertFalse(captor.getValue().isClearImage());
        }

        @Test
        @DisplayName("returns OK and details when service succeeds")
        void returnsOkWhenServiceSucceeds() throws Exception {
            when(userService.updateProfile(any()))
                    .thenReturn(new UserPresentableView(mockUser));

            mockMvc.perform(patch(baseEndpoint + "/profile")
                            .param("firstname", "test")
                            .contentType("multipart/form-data"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("id").value(mockUser.getId().toString()));
        }

        @Test
        @DisplayName("returns error status code when thrown by service")
        void returnsErrorStatusCodeWhenThrownByService() throws Exception {
            when(userService.updateProfile(any()))
                    .thenThrow(new ServiceException(HttpStatus.NOT_FOUND, ""));

            mockMvc.perform(patch(baseEndpoint + "/profile")
                            .contentType("multipart/form-data"))
                    .andExpect(status().isNotFound());
        }
    }
}
























