package pl.mwasyluk.ouroom_server.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import lombok.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import pl.mwasyluk.ouroom_server.domain.media.Media;
import pl.mwasyluk.ouroom_server.domain.media.source.DataSource;
import pl.mwasyluk.ouroom_server.domain.media.source.ExternalDataSource;
import pl.mwasyluk.ouroom_server.exceptions.ServiceException;
import pl.mwasyluk.ouroom_server.services.media.MediaService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.mwasyluk.ouroom_server.domain.media.source.DataSourceTestUtil.JPEG_BYTES;

@WebMvcTest(value = MediaController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private MediaService mediaService;

    private String baseEndpoint;

    @Value("${server.api.prefix}")
    public void setBaseEndpoint(String apiPrefix) {
        this.baseEndpoint = apiPrefix + "/media";
    }

    private static class MockMediaWithInternalUrl extends Media {
        public MockMediaWithInternalUrl(DataSource dataSource) {
            super(dataSource);
        }

        @Override
        protected void validate() {
        }

        @Override
        public @NonNull String getInternalUrl() {
            return "http://localhost:8080/api/media/" + getId();
        }
    }

    @Nested
    @DisplayName("GET /api/media")
    class ReadByIdMethodTest {
        @Test
        @DisplayName("returns NOT_FOUND when mediaId path variable is not provided")
        void returnsBadRequestWhenMediaIdPathVariableIsNotProvided() throws Exception {
            mockMvc.perform(get(baseEndpoint + "/"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns BAD_REQUEST when provided uuid is invalid")
        void returnsBadRequestWhenProvidedUuidIsInvalid() throws Exception {
            mockMvc.perform(get(baseEndpoint + "/acdd6evg1-87e5-4647-b1b1-8b1b2468dbd1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns PERMANENT_REDIRECT when media source is external")
        void returnsPermanentRedirectWhenMediaSourceIsExternal() throws Exception {
            ExternalDataSource mockExternalDataSource =
                    (ExternalDataSource) DataSource.of("http://api.google.com/media/img.jpg");
            Media mockExternalMedia =
                    new MockMediaWithInternalUrl(mockExternalDataSource);
            when(mediaService.read(mockExternalMedia.getId()))
                    .thenReturn(mockExternalMedia);

            mockMvc.perform(get(baseEndpoint + "/" + mockExternalMedia.getId()))
                    .andExpect(status().isPermanentRedirect())
                    .andExpect(redirectedUrl(mockExternalDataSource.getUrl().toString()));
        }

        @Test
        @DisplayName("returns OK when media source is internal")
        void returnsOkWhenMediaSourceIsInternal() throws Exception {
            Media mockInternalMedia =
                    new MockMediaWithInternalUrl(DataSource.of(JPEG_BYTES));
            when(mediaService.read(mockInternalMedia.getId()))
                    .thenReturn(mockInternalMedia);

            mockMvc.perform(get(baseEndpoint + "/" + mockInternalMedia.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                    .andExpect(content().bytes(JPEG_BYTES));
        }

        @Test
        @DisplayName("returns error status code when exception thrown by service")
        void returnsErrorStatusCodeWhenExceptionThrownByService() throws Exception {
            when(mediaService.read(any()))
                    .thenThrow(new ServiceException(HttpStatus.NOT_FOUND, ""));

            mockMvc.perform(get(baseEndpoint + "/" + UUID.randomUUID()))
                    .andExpect(status().isNotFound());
        }
    }
}
