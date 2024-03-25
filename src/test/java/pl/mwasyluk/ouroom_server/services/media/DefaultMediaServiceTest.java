package pl.mwasyluk.ouroom_server.services.media;

import java.util.Optional;
import java.util.UUID;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.multipart.MultipartFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.mwasyluk.ouroom_server.domain.media.Media;
import pl.mwasyluk.ouroom_server.domain.media.source.DataSource;
import pl.mwasyluk.ouroom_server.exceptions.ServiceException;
import pl.mwasyluk.ouroom_server.mocks.WithMockCustomUser;
import pl.mwasyluk.ouroom_server.repos.MediaRepository;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static pl.mwasyluk.ouroom_server.domain.media.source.DataSourceTestUtil.JPEG_BYTES;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig
class DefaultMediaServiceTest {
    @Mock
    private MediaRepository mediaRepository;

    private DefaultMediaService mediaService;
    private Media mockMedia;

    @BeforeEach
    void setUp() {
        mediaService = new DefaultMediaService(mediaRepository);
        mockMedia = Media.of(DataSource.of(JPEG_BYTES));
    }

    @Nested
    @DisplayName("read method")
    class ReadMethodTest {
        @Test
        @DisplayName("throws UNAUTHORIZED when user is not authenticated")
        void throwsUnauthorizedWhenUserIsNotAuthenticated() {
            ServiceException serviceException =
                    assertThrowsExactly(ServiceException.class, () -> mediaService.read(UUID.randomUUID()));
            assertEquals(UNAUTHORIZED, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws NOT_FOUND when media does not exist")
        void throwsNotFoundWhenMediaDoesNotExist() {
            UUID mediaId = UUID.randomUUID();

            when(mediaRepository.findById(mediaId)).thenReturn(Optional.empty());

            ServiceException serviceException =
                    assertThrowsExactly(ServiceException.class, () -> mediaService.read(mediaId));
            assertEquals(NOT_FOUND, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("returns media when user is authenticated")
        void returnsMediaWhenUserIsAuthenticated() {
            UUID mediaId = UUID.randomUUID();

            when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(mockMedia));

            Media media = assertDoesNotThrow(() -> mediaService.read(mediaId));
            assertEquals(mockMedia, media);
        }
    }

    @Nested
    @DisplayName("create method")
    class CreateMethodTest {
        @Test
        @DisplayName("throws UNAUTHORIZED when user is not authenticated")
        void throwsUnauthorizedWhenUserIsNotAuthenticated() {
            ServiceException serviceException =
                    assertThrowsExactly(ServiceException.class, () -> mediaService.create(null));
            assertEquals(UNAUTHORIZED, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws UNPROCESSABLE_ENTITY when file is null")
        void throwsUnprocessableEntityWhenFileIsNull() {
            ServiceException serviceException =
                    assertThrowsExactly(ServiceException.class, () -> mediaService.create(null));
            assertEquals(UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws UNPROCESSABLE_ENTITY when file is empty")
        void throwsUnprocessableEntityWhenFileIsEmpty() {
            MultipartFile mockFile = new MockMultipartFile("file.png", new byte[0]);

            ServiceException serviceException =
                    assertThrowsExactly(ServiceException.class, () -> mediaService.create(mockFile));
            assertEquals(UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("throws UNPROCESSABLE_ENTITY when file byte array is not valid media file")
        void throwsUnprocessableEntityWhenFileByteArrayIsNotValidMediaFile() {
            MultipartFile mockFile = new MockMultipartFile("file.png", new byte[]{0x00, 0x01, 0x02, 0x03});

            ServiceException serviceException =
                    assertThrowsExactly(ServiceException.class, () -> mediaService.create(mockFile));
            assertEquals(UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("saves and returns media when user is authenticated")
        void savesAndReturnsMediaWhenUserIsAuthenticated() {
            MultipartFile mockFile = new MockMultipartFile("file.jpg", JPEG_BYTES);

            when(mediaRepository.save(any())).thenReturn(mockMedia);

            Media media = assertDoesNotThrow(() -> mediaService.create(mockFile));
            assertEquals(mockMedia, media);

            ArgumentCaptor<Media> argument = ArgumentCaptor.forClass(Media.class);
            verify(mediaRepository).save(argument.capture());
            assertArrayEquals(JPEG_BYTES, argument.getValue().getSource().getData());
        }
    }
}
