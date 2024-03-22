package pl.mwasyluk.ouroom_server.services.user;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.mwasyluk.ouroom_server.domain.media.Image;
import pl.mwasyluk.ouroom_server.domain.media.Media;
import pl.mwasyluk.ouroom_server.domain.media.source.DataSource;
import pl.mwasyluk.ouroom_server.domain.media.source.DataSourceTestUtil;
import pl.mwasyluk.ouroom_server.domain.user.User;
import pl.mwasyluk.ouroom_server.domain.user.UserAuthority;
import pl.mwasyluk.ouroom_server.dto.user.UserDetailsForm;
import pl.mwasyluk.ouroom_server.dto.user.UserDetailsView;
import pl.mwasyluk.ouroom_server.dto.user.UserPresentableForm;
import pl.mwasyluk.ouroom_server.dto.user.UserPresentableView;
import pl.mwasyluk.ouroom_server.exceptions.ServiceException;
import pl.mwasyluk.ouroom_server.mocks.WithMockCustomUser;
import pl.mwasyluk.ouroom_server.repos.UserRepository;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pl.mwasyluk.ouroom_server.mocks.WithUserDetailsSecurityContextFactory.pullPrincipalUser;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig
public class DefaultUserServiceTest {
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Mock
    private UserRepository userRepository;
    private DefaultUserService defaultUserService;
    private User mockUser;

    @BeforeEach
    void setUp() {
        defaultUserService = new DefaultUserService(userRepository);
        mockUser = new User("test", "pass", Set.of(UserAuthority.USER));
    }

    @Nested
    @DisplayName("create method")
    class CreateMethod {
        // test if create method throws exception when either email or password is null
        @Test
        @DisplayName("throws exception when either email or password is null")
        void throwsExceptionWhenEitherEmailOrPasswordIsNull() {
            UserDetailsForm userDetailsForm1 = new UserDetailsForm();
            userDetailsForm1.setEmail("email");
            UserDetailsForm userDetailsForm2 = new UserDetailsForm();
            userDetailsForm2.setPassword("password");

            assertAll(() -> {
                ServiceException serviceException1 =
                        assertThrows(ServiceException.class, () -> defaultUserService.create(userDetailsForm1));
                ServiceException serviceException2 =
                        assertThrows(ServiceException.class, () -> defaultUserService.create(userDetailsForm2));
                assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, serviceException1.getStatusCode());
                assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, serviceException2.getStatusCode());
            });
        }

        // test if create method throws exception when user with given email already exists
        @Test
        @DisplayName("throws exception when user with given email already exists")
        void throwsExceptionWhenUserWithGivenEmailAlreadyExists() {
            UserDetailsForm userDetailsForm = new UserDetailsForm();
            userDetailsForm.setEmail("email");
            userDetailsForm.setPassword("password");

            when(userRepository.findByUsername("email")).thenReturn(Optional.of(mockUser));

            assertAll(() -> {
                ServiceException serviceException =
                        assertThrows(ServiceException.class, () -> defaultUserService.create(userDetailsForm));
                assertEquals(HttpStatus.CONFLICT, serviceException.getStatusCode());

                verify(userRepository).findByUsername("email");
            });
        }

        // test if create method throws exception when user password is plain text
        @Test
        @DisplayName("throws exception when user password is plain text")
        void throwsExceptionWhenUserPasswordIsPlainText() {
            UserDetailsForm userDetailsForm = new UserDetailsForm();
            userDetailsForm.setEmail("email");
            userDetailsForm.setPassword("password");

            assertAll(() -> {
                ServiceException serviceException =
                        assertThrows(ServiceException.class, () -> defaultUserService.create(userDetailsForm));
                assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
            });
        }

        // test if create method throws exception when user password is bcrypt hash but without prefix
        @Test
        @DisplayName("throws exception when user password is prefixed plain text")
        void throwsExceptionWhenUserPasswordIsPrefixedPlainText() {
            UserDetailsForm userDetailsForm = new UserDetailsForm();
            userDetailsForm.setEmail("email");
            userDetailsForm.setPassword("{bcrypt}" + "password");

            assertAll(() -> {
                ServiceException serviceException =
                        assertThrows(ServiceException.class, () -> defaultUserService.create(userDetailsForm));
                assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
            });
        }

        // test if create method throws exception when user password is bcrypt hash but without prefix
        @Test
        @DisplayName("throws exception when user password is bcrypt hash but without prefix")
        void throwsExceptionWhenUserPasswordIsBcryptHashButWithoutPrefix() {
            UserDetailsForm userDetailsForm = new UserDetailsForm();
            userDetailsForm.setEmail("email");
            userDetailsForm.setPassword(passwordEncoder.encode("password"));

            assertAll(() -> {
                ServiceException serviceException =
                        assertThrows(ServiceException.class, () -> defaultUserService.create(userDetailsForm));
                assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
            });
        }

        // test if create method returns UserDetailsView object when user is created successfully
        @Test
        @DisplayName("returns UserDetailsView object when user is created successfully")
        void returnsUserDetailsViewObjectWhenUserIsCreatedSuccessfully() {
            UserDetailsForm userDetailsForm = new UserDetailsForm();
            userDetailsForm.setEmail("email");
            userDetailsForm.setPassword("{bcrypt}" + passwordEncoder.encode("password"));

            when(userRepository.save(any())).thenReturn(mockUser);

            assertAll(() -> {
                UserDetailsView userDetailsView
                        = assertDoesNotThrow(() -> defaultUserService.create(userDetailsForm));
                assertNotNull(userDetailsView);

                ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(argument.capture());
                assertEquals(userDetailsForm.getEmail(), argument.getValue().getUsername());
                assertEquals(userDetailsForm.getPassword(), argument.getValue().getPassword());
            });
        }
    }

    @Nested
    @DisplayName("readDetails method")
    class ReadDetailsMethod {
        // test if readDetails method throws exception when user is not authenticated
        @Test
        @DisplayName("throws exception when user is not authenticated")
        void throwsExceptionWhenUserIsNotAuthenticated() {
            assertAll(() -> {
                ServiceException serviceException =
                        assertThrows(ServiceException.class, () -> defaultUserService.readDetails(null));
                assertEquals(HttpStatus.UNAUTHORIZED, serviceException.getStatusCode());
            });
        }

        // test if readDetails method does not throw exception when user is authenticated
        @Test
        @DisplayName("does not throw exception when user is authenticated and null provided")
        @WithMockCustomUser
        void doesNotThrowExceptionWhenUserIsAuthenticatedAndNullProvided() {
            assertDoesNotThrow(() -> defaultUserService.readDetails(null));
        }

        // test if readDetails method returns UserDetailsView object when user is authenticated and null provided
        @Test
        @DisplayName("returns UserDetailsView object when user is authenticated and null provided")
        @WithMockCustomUser
        void returnsUserDetailsViewObjectWhenUserIsAuthenticatedAndNullProvided() {
            assertAll(() -> {
                UserDetailsView userDetailsView
                        = assertDoesNotThrow(() -> defaultUserService.readDetails(null));
                assertNotNull(userDetailsView);
            });
        }

        // test if readDetails method throws exception when user is not owner user or admin
        @Test
        @DisplayName("throws exception when user is not admin and not owner id provided")
        @WithMockCustomUser
        void throwsExceptionWhenUserIsNotAdminAndNotOwnerIdProvided() {
            assertAll(() -> {
                ServiceException serviceException =
                        assertThrows(ServiceException.class,
                                () -> defaultUserService.readDetails(UUID.randomUUID()));
                assertEquals(HttpStatus.FORBIDDEN, serviceException.getStatusCode());
            });
        }

        // //

        // test if readDetails method throws exception when user with given ID does not exist
        @Test
        @DisplayName("throws exception when user with given ID does not exist and admin authenticated")
        @WithMockCustomUser("admin")
        void throwsExceptionWhenUserWithGivenIdDoesNotExistAndAdminAuthenticated() {
            UUID userId = UUID.randomUUID();

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertAll(() -> {
                ServiceException serviceException =
                        assertThrows(ServiceException.class, () -> defaultUserService.readDetails(userId));
                assertEquals(HttpStatus.NOT_FOUND, serviceException.getStatusCode());
                verify(userRepository).findById(userId);
            });
        }

        // test if readDetails method throws exception when user is not owner user or admin
        @Test
        @DisplayName("throws exception when user is not owner user or admin")
        @WithMockCustomUser
        void throwsExceptionWhenUserIsNotOwnerUserOrAdmin() {
            assertAll(() -> {
                ServiceException serviceException =
                        assertThrows(ServiceException.class, () -> defaultUserService.readDetails(mockUser.getId()));
                assertEquals(HttpStatus.FORBIDDEN, serviceException.getStatusCode());
            });
        }

        // test if readDetails method returns UserDetailsView object when user is owner user
        @Test
        @DisplayName("returns UserDetailsView object when user is owner user")
        @WithMockCustomUser
        void returnsUserDetailsViewObjectWhenUserIsOwnerUser() {
            User principal = pullPrincipalUser();

            when(userRepository.findById(principal.getId())).thenReturn(Optional.of(principal));

            assertAll(() -> {
                UserDetailsView userDetailsView
                        = assertDoesNotThrow(() -> defaultUserService.readDetails(principal.getId()));
                assertNotNull(userDetailsView);
            });
        }

        // test if readDetails method returns UserDetailsView object when user is admin
        @Test
        @DisplayName("returns UserDetailsView object when user is admin")
        @WithMockCustomUser("admin")
        void returnsUserDetailsViewObjectWhenUserIsAdmin() {
            User principal = pullPrincipalUser();

            when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));

            assertAll(() -> {
                UserDetailsView userDetailsView
                        = assertDoesNotThrow(() -> defaultUserService.readDetails(mockUser.getId()));
                assertNotNull(userDetailsView);
                assertTrue(principal.getAuthorities().contains(UserAuthority.ADMIN));
            });
        }
    }

    @Nested
    @DisplayName("readPresentable method")
    class ReadPresentableMethod {
        // test if readPresentable method throws exception when user is not authenticated
        @Test
        @DisplayName("throws UNAUTHORIZED when user is not authenticated")
        void throwsUnauthorizedWhenUserIsNotAuthenticated() {
            assertAll(() -> {
                ServiceException serviceException =
                        assertThrows(ServiceException.class, () -> defaultUserService.readPresentable(null));
                assertEquals(HttpStatus.UNAUTHORIZED, serviceException.getStatusCode());
            });
        }

        // test if readPresentable method returns UserPresentableView object when user is authenticated and null provided
        @Test
        @DisplayName("returns UserPresentableView object when user is authenticated and null provided")
        @WithMockCustomUser
        void returnsUserPresentableViewObjectWhenUserIsAuthenticatedAndNullProvided() {
            assertAll(() -> {
                UserPresentableView userPresentableView
                        = assertDoesNotThrow(() -> defaultUserService.readPresentable(null));
                assertNotNull(userPresentableView);
            });
        }

        // test if readPresentable method throws exception when non-existent user id provided
        @Test
        @DisplayName("throws NOT_FOUND when non-existent user id provided")
        @WithMockCustomUser
        void throwsNotFoundWhenNonExistentUserIdProvided() {
            UUID userId = UUID.randomUUID();

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertAll(() -> {
                ServiceException serviceException =
                        assertThrows(ServiceException.class, () -> defaultUserService.readPresentable(userId));
                assertEquals(HttpStatus.NOT_FOUND, serviceException.getStatusCode());

                verify(userRepository).findById(userId);
            });
        }

        // test if readPresentable method returns UserPresentableView object when user with given ID exists
        @Test
        @DisplayName("returns UserPresentableView object when user with given ID exists")
        @WithMockCustomUser
        void returnsUserPresentableViewObjectWhenUserWithGivenIdExists() {
            when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));

            assertAll(() -> {
                UserPresentableView userPresentableView
                        = assertDoesNotThrow(() -> defaultUserService.readPresentable(mockUser.getId()));
                assertNotNull(userPresentableView);

                verify(userRepository).findById(mockUser.getId());
            });
        }
    }

    @Nested
    @SpringBootTest
    @ActiveProfiles("test")
    @DisplayName("updateProfile method")
    class UpdateProfileMethod {
        Image mockImage = (Image) Media.of(DataSource.of(DataSourceTestUtil.JPEG_BYTES));
        MockMultipartFile mockImageFile = new MockMultipartFile("mock_image.jpeg", mockImage.getSource().getData());

        // test if updateProfile method throws exception when user is not authenticated
        @Test
        @DisplayName("throws UNAUTHORIZED when user is not authenticated")
        void throwsUnauthorizedWhenUserIsNotAuthenticated() {
            assertAll(() -> {
                ServiceException serviceException = assertThrows(ServiceException.class,
                        () -> defaultUserService.updateProfile(new UserPresentableForm()));
                assertEquals(HttpStatus.UNAUTHORIZED, serviceException.getStatusCode());
            });
        }

        // test if updateProfile method throws exception when user has no profile name and null name provided
        @Test
        @DisplayName("throws UNPROCESSABLE_ENTITY when user has no profile name and incomplete name provided")
        @WithMockCustomUser
        void throwsUnprocessableEntityWhenUserHasNoProfileNameAndIncompleteNameProvided() {
            UserPresentableForm form = new UserPresentableForm();
            form.setLastname(" Tsdf");
            form.setLastname("  ");
            UserPresentableForm form2 = new UserPresentableForm();
            form2.setFirstname(null);
            form2.setLastname("Lastname");
            UserPresentableForm form3 = new UserPresentableForm();
            form3.setFirstname("  \n ");
            form3.setLastname("Lastname");

            assertAll(() -> {
                ServiceException serviceException =
                        assertThrows(ServiceException.class, () -> defaultUserService.updateProfile(form));
                assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
                ServiceException serviceException2 =
                        assertThrows(ServiceException.class, () -> defaultUserService.updateProfile(form2));
                assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, serviceException2.getStatusCode());
                ServiceException serviceException3 =
                        assertThrows(ServiceException.class, () -> defaultUserService.updateProfile(form3));
                assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, serviceException3.getStatusCode());
            });
        }

        // test if updateProfile method sets only provided fields
        @Test
        @DisplayName("sets only provided fields")
        @WithMockCustomUser
        void setsOnlyProvidedFields() {
            User principal = pullPrincipalUser();
            principal.profileBuilder().setFirstname("Firstname").setLastname("Lastname").setImage(mockImage).apply();

            UserPresentableForm form = new UserPresentableForm();
            form.setFirstname("NewFirstname");
            form.setLastname("  \n ");

            when(userRepository.save(any())).thenReturn(principal);
            defaultUserService.updateProfile(form);

            assertAll(() -> {
                ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(argument.capture());
                assertEquals("NewFirstname Lastname", argument.getValue().getName());
                assertEquals(mockImage, argument.getValue().getImage());
            });
        }

        // test if updateProfile method clears image when clearImage is true
        @Test
        @DisplayName("clears image when clearImage is true")
        @WithMockCustomUser
        void clearsImageWhenClearImageIsTrue() {
            User principal = pullPrincipalUser();
            principal.profileBuilder().setFirstname("Firstname").setLastname("Lastname").setImage(mockImage).apply();

            UserPresentableForm form = new UserPresentableForm();
            form.setClearImage(true);

            when(userRepository.save(any())).thenReturn(principal);
            defaultUserService.updateProfile(form);

            assertAll(() -> {
                ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(argument.capture());
                assertEquals("Firstname Lastname", argument.getValue().getName());
                assertNull(argument.getValue().getImage());
            });
        }

        // test if updateProfile method clears image when clearImage is true and image is provided
        @Test
        @DisplayName("clears image when clearImage is true and image is provided")
        @WithMockCustomUser
        void clearsImageWhenClearImageIsTrueAndImageIsProvided() {
            User principal = pullPrincipalUser();
            principal.profileBuilder().setFirstname("Firstname").setLastname("Lastname").setImage(mockImage).apply();

            UserPresentableForm form = new UserPresentableForm();
            form.setClearImage(true);
            form.setFile(mockImageFile);

            when(userRepository.save(any())).thenReturn(principal);
            defaultUserService.updateProfile(form);

            assertAll(() -> {
                ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(argument.capture());
                assertEquals("Firstname Lastname", argument.getValue().getName());
                assertNull(argument.getValue().getImage());
            });
        }

        // test if updateProfile method sets image when image is provided and clearImage is false
        @Test
        @DisplayName("sets image when image is provided and clearImage is false")
        @WithMockCustomUser
        void setsImageWhenImageIsProvidedAndClearImageIsFalse() {
            User principal = pullPrincipalUser();
            principal.profileBuilder().setFirstname("Firstname").setLastname("Lastname").setImage(mockImage).apply();

            UserPresentableForm form = new UserPresentableForm();
            form.setClearImage(false);
            form.setFile(mockImageFile);

            when(userRepository.save(any())).thenReturn(principal);
            defaultUserService.updateProfile(form);

            assertAll(() -> {
                ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(argument.capture());
                assertEquals("Firstname Lastname", argument.getValue().getName());
                assertArrayEquals(mockImageFile.getBytes(),
                        argument.getValue().getImage().getDataSource().getData());
            });
        }

        // test if updateProfile method overrides image when another image is provided
        @Test
        @DisplayName("overrides image when another image is provided")
        @WithMockCustomUser
        void overridesImageWhenAnotherImageIsProvided() {
            User principal = pullPrincipalUser();
            principal.profileBuilder().setFirstname("Firstname").setLastname("Lastname").setImage(mockImage).apply();

            Image mockImage2 = (Image) Media.of(DataSource.of(DataSourceTestUtil.PNG_BYTES));
            MockMultipartFile mockImageFile2 =
                    new MockMultipartFile("mock_image.png", mockImage2.getSource().getData());

            UserPresentableForm form = new UserPresentableForm();
            form.setClearImage(false);
            form.setFile(mockImageFile2);

            when(userRepository.save(any())).thenReturn(principal);
            defaultUserService.updateProfile(form);

            assertAll(() -> {
                ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(argument.capture());
                assertEquals("Firstname Lastname", argument.getValue().getName());
                assertArrayEquals(mockImage2.getSource().getData(),
                        argument.getValue().getImage().getDataSource().getData());
            });
        }

        // test if updateProfile method throws exception when file is not a valid image
        @Test
        @DisplayName("throws UNPROCESSABLE_ENTITY when file is not a valid image")
        @WithMockCustomUser
        void throwsUnprocessableEntityWhenFileIsNotAValidImage() {
            User principal = pullPrincipalUser();
            principal.profileBuilder().setFirstname("Firstname").setLastname("Lastname").setImage(mockImage).apply();

            UserPresentableForm form = new UserPresentableForm();
            form.setFile(new MockMultipartFile("mock_file.txt", "text".getBytes()));

            assertAll(() -> {
                ServiceException serviceException =
                        assertThrows(ServiceException.class, () -> defaultUserService.updateProfile(form));
                assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
            });
        }

        // test if updateProfile method returns UserPresentableView object when user profile is updated successfully
        @Test
        @DisplayName("returns UserPresentableView object when user profile is updated successfully")
        @WithMockCustomUser
        void returnsUserPresentableViewObjectWhenUserProfileIsUpdatedSuccessfully() {
            User principal = pullPrincipalUser();
            principal.profileBuilder().setFirstname("Firstname").setLastname("Lastname").setImage(mockImage).apply();

            UserPresentableForm form = new UserPresentableForm();
            form.setFirstname("NewFirstname");
            form.setLastname("NewLastname");
            form.setClearImage(true);

            when(userRepository.save(any())).thenReturn(principal);

            assertAll(() -> {
                UserPresentableView userPresentableView
                        = assertDoesNotThrow(() -> defaultUserService.updateProfile(form));
                assertNotNull(userPresentableView);
            });
        }
    }

    @Nested
    @DisplayName("updateAccount method")
    class UpdateAccountMethod {
        // test if updateAccount method throws exception when user is not authenticated
        @Test
        @DisplayName("throws UNAUTHORIZED when user is not authenticated")
        void throwsUnauthorizedWhenUserIsNotAuthenticated() {
            assertAll(() -> {
                ServiceException serviceException = assertThrows(ServiceException.class,
                        () -> defaultUserService.updateAccount(new UserDetailsForm()));
                assertEquals(HttpStatus.UNAUTHORIZED, serviceException.getStatusCode());
            });
        }

        // test if updateAccount method sets only provided fields
        @Test
        @DisplayName("sets only provided fields")
        @WithMockCustomUser
        void setsOnlyProvidedFields() {
            User principal = pullPrincipalUser();
            principal.accountBuilder().setUsername("Username").setPassword("Password").apply();

            UserDetailsForm form = new UserDetailsForm();
            form.setEmail("NewEmail");
            form.setPassword("  \n ");
            form.setAuthorities(null);

            when(userRepository.save(any())).thenReturn(principal);
            defaultUserService.updateAccount(form);

            assertAll(() -> {
                ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(argument.capture());
                assertEquals("NewEmail", argument.getValue().getUsername());
                assertEquals("Password", argument.getValue().getPassword());
                assertArrayEquals(new Object[]{UserAuthority.USER}, argument.getValue().getAuthorities().toArray());
            });
        }

        // test if updateAccount method throws exception when user with given email already exists
        @Test
        @DisplayName("throws CONFLICT when user with given email already exists")
        @WithMockCustomUser
        void throwsConflictWhenUserWithGivenEmailAlreadyExists() {
            User principal = pullPrincipalUser();
            principal.accountBuilder().setUsername("Username").setPassword("Password").apply();

            UserDetailsForm form = new UserDetailsForm();
            form.setEmail("NewEmail");
            form.setPassword("Password");

            when(userRepository.findByUsername("NewEmail")).thenReturn(Optional.of(mockUser));

            assertAll(() -> {
                ServiceException serviceException =
                        assertThrows(ServiceException.class, () -> defaultUserService.updateAccount(form));
                assertEquals(HttpStatus.CONFLICT, serviceException.getStatusCode());

                verify(userRepository).findByUsername("NewEmail");
            });
        }

        // test if updateAccount method throws exception when user password is plain text
        @Test
        @DisplayName("throws UNPROCESSABLE_ENTITY when user password is plain text")
        @WithMockCustomUser
        void throwsUnprocessableEntityWhenUserPasswordIsPlainText() {
            User principal = pullPrincipalUser();
            principal.accountBuilder().setUsername("Username").setPassword("Password").apply();

            UserDetailsForm form = new UserDetailsForm();
            form.setEmail("NewEmail");
            form.setPassword("password");

            assertAll(() -> {
                ServiceException serviceException =
                        assertThrows(ServiceException.class, () -> defaultUserService.updateAccount(form));
                assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, serviceException.getStatusCode());
            });
        }

        // test if updateAccount method returns UserDetailsView object when user account is updated successfully
        @Test
        @DisplayName("returns UserDetailsView object when user account is updated successfully")
        @WithMockCustomUser
        void returnsUserDetailsViewObjectWhenUserAccountIsUpdatedSuccessfully() {
            User principal = pullPrincipalUser();
            principal.accountBuilder().setUsername("Username").setPassword("Password").apply();

            UserDetailsForm form = new UserDetailsForm();
            form.setEmail("NewEmail");
            String newPassword = "{bcrypt}" + passwordEncoder.encode("NewPassword");
            form.setPassword(newPassword);

            when(userRepository.findByUsername("NewEmail")).thenReturn(Optional.empty());
            when(userRepository.save(any())).thenReturn(principal);

            assertAll(() -> {
                UserDetailsView userDetailsView
                        = assertDoesNotThrow(() -> defaultUserService.updateAccount(form));
                assertNotNull(userDetailsView);

                ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(argument.capture());
                assertEquals("NewEmail", argument.getValue().getUsername());
                assertEquals(newPassword, argument.getValue().getPassword());
            });
        }
    }

    @Nested
    @DisplayName("disable method")
    class DisableMethod {
        // test if disable method throws exception when user is not authenticated
        @Test
        @DisplayName("throws UNAUTHORIZED when user is not authenticated")
        void throwsUnauthorizedWhenUserIsNotAuthenticated() {
            assertAll(() -> {
                ServiceException serviceException = assertThrows(ServiceException.class,
                        () -> defaultUserService.disable(null));
                assertEquals(HttpStatus.UNAUTHORIZED, serviceException.getStatusCode());
            });
        }

        // test if disable method throws exception when user is not owner user or admin
        @Test
        @DisplayName("throws FORBIDDEN when user is not owner user or admin")
        @WithMockCustomUser
        void throwsForbiddenWhenUserIsNotOwnerUserOrAdmin() {
            assertAll(() -> {
                ServiceException serviceException =
                        assertThrows(ServiceException.class, () -> defaultUserService.disable(UUID.randomUUID()));
                assertEquals(HttpStatus.FORBIDDEN, serviceException.getStatusCode());
            });
        }

        // test if disable method throws exception when user with given ID does not exist
        @Test
        @DisplayName("throws NOT_FOUND when user with given ID does not exist and admin authenticated")
        @WithMockCustomUser("admin")
        void throwsNotFoundWhenUserWithGivenIdDoesNotExistAndAdminAuthenticated() {
            UUID userId = UUID.randomUUID();

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertAll(() -> {
                ServiceException serviceException =
                        assertThrows(ServiceException.class, () -> defaultUserService.disable(userId));
                assertEquals(HttpStatus.NOT_FOUND, serviceException.getStatusCode());
                verify(userRepository).findById(userId);
            });
        }

        // test if disable method throws exception when admin tries to disable admin user
        @Test
        @DisplayName("throws FORBIDDEN when admin tries to disable admin user")
        @WithMockCustomUser("admin")
        void throwsForbiddenWhenAdminTriesToDisableAdminUser() {
            User adminUser = new User("admin", "pass", Set.of(UserAuthority.ADMIN, UserAuthority.USER));

            when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));

            assertAll(() -> {
                ServiceException serviceException =
                        assertThrows(ServiceException.class, () -> defaultUserService.disable(adminUser.getId()));
                assertEquals(HttpStatus.FORBIDDEN, serviceException.getStatusCode());
            });
        }

        // test if disable method disables user when user is owner user
        @Test
        @DisplayName("disables user when user is owner user")
        @WithMockCustomUser
        void disablesUserWhenUserIsOwnerUser() {
            User principal = pullPrincipalUser();
            assert principal.isEnabled();

            when(userRepository.save(any())).thenReturn(principal);

            assertAll(() -> {
                assertDoesNotThrow(() -> defaultUserService.disable(principal.getId()));

                ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(argument.capture());
                assertFalse(argument.getValue().isEnabled());
            });
        }

        // test if disable method disables user when principal is admin
        @Test
        @DisplayName("disables user when principal is admin")
        @WithMockCustomUser("admin")
        void disablesUserWhenPrincipalIsAdmin() {
            User targetUser = new User("target", "pass", Set.of(UserAuthority.USER));
            assert targetUser.isEnabled();

            when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
            when(userRepository.save(any())).thenReturn(targetUser);

            assertAll(() -> {
                assertDoesNotThrow(() -> defaultUserService.disable(targetUser.getId()));

                ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(argument.capture());
                assertFalse(argument.getValue().isEnabled());
            });
        }

        // test if disable method disables principal when null provided
        @Test
        @DisplayName("disables principal when null provided")
        @WithMockCustomUser
        void disablesPrincipalWhenNullProvided() {
            User principal = pullPrincipalUser();
            assert principal.isEnabled();

            when(userRepository.save(any())).thenReturn(principal);

            assertAll(() -> {
                assertDoesNotThrow(() -> defaultUserService.disable(null));

                ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(argument.capture());
                assertFalse(argument.getValue().isEnabled());
            });
        }
    }

    @Nested
    @DisplayName("loadUserByUsername method")
    class LoadUserByUsernameMethod {
        // test if loadUserByUsername method throws exception when user with given email does not exist
        @Test
        @DisplayName("throws UsernameNotFoundException when user with given email does not exist")
        void throwsUsernameNotFoundExceptionWhenUserWithGivenEmailDoesNotExist() {
            when(userRepository.findByUsername("email")).thenReturn(Optional.empty());

            assertAll(() -> {
                assertThrows(UsernameNotFoundException.class, () -> defaultUserService.loadUserByUsername("email"));

                verify(userRepository).findByUsername("email");
            });
        }

        // test if loadUserByUsername method returns User object when user with given email exists
        @Test
        @DisplayName("returns User object when user with given email exists")
        void returnsUserObjectWhenUserWithGivenEmailExists() {
            when(userRepository.findByUsername("email")).thenReturn(Optional.of(mockUser));

            assertAll(() -> {
                User user = assertDoesNotThrow(() -> defaultUserService.loadUserByUsername("email"));
                assertNotNull(user);
            });
        }

        // throws null pointer exception when null email provided
        @Test
        @DisplayName("throws NullPointerException when null email provided")
        void throwsNullPointerExceptionWhenNullEmailProvided() {
            assertThrows(NullPointerException.class, () -> defaultUserService.loadUserByUsername(null));
        }
    }
}
