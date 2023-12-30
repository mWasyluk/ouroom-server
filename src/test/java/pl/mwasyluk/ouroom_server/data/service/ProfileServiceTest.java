package pl.mwasyluk.ouroom_server.data.service;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.UUID;

import org.springframework.format.datetime.DateFormatter;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.mwasyluk.ouroom_server.data.repository.AccountRepository;
import pl.mwasyluk.ouroom_server.data.repository.ProfileRepository;
import pl.mwasyluk.ouroom_server.data.service.support.ServiceResponse;
import pl.mwasyluk.ouroom_server.data.service.support.ServiceResponseMessages;
import pl.mwasyluk.ouroom_server.domain.userdetails.Account;
import pl.mwasyluk.ouroom_server.domain.userdetails.Profile;
import pl.mwasyluk.ouroom_server.domain.userdetails.ProfileAvatar;
import pl.mwasyluk.ouroom_server.util.UuidUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pl.mwasyluk.ouroom_server.data.service.support.ServiceResponse.INCORRECT_ID;
import static pl.mwasyluk.ouroom_server.data.service.support.ServiceResponse.UNAUTHORIZED;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {
    static String testFirstName = "Test";
    static String testLastName = "Testy";
    static Date testBirthdate = new Date();
    Profile testProfile;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private AccountRepository accountRepository;
    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(profileRepository, accountRepository);
        testProfile = new Profile(testFirstName, testLastName, testBirthdate);
    }

    @Nested
    @DisplayName("Test if the getProfileById() method")
    class GetProfileByIdMethodTest {
        @Test
        @DisplayName("returns the Profile when it exists with the given UUID")
        void returnsTheProfileWhenItExistsWithTheGivenId() {
            Account account = new Account();
            account.setProfile(testProfile);
            when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

            ServiceResponse<?> serviceResponse = profileService.getProfileByAccountId(account.getId());

            assertThat(serviceResponse.getBody()).isEqualTo(testProfile);
        }

        @Test
        @DisplayName("returns INCORRECT_ID when the profile with the given UUID does not exist")
        void returnsNull() {
            ServiceResponse<?> serviceResponse = profileService.getProfileByAccountId(testProfile.getId());

            assertThat(serviceResponse).isEqualTo(INCORRECT_ID);
        }

        @Test
        @DisplayName("returns the Profile when it exists with the given UUID as a String")
        void returnsTheProfileWhenItExistsWithTheGivenUuidAsAString() {
            when(profileRepository.findById(testProfile.getId())).thenReturn(Optional.of(testProfile));

            ServiceResponse<?> serviceResponse = profileService.getProfileById(testProfile.getId().toString());

            assertThat(serviceResponse.getBody()).isEqualTo(testProfile);
        }

        @Test
        @DisplayName("returns INCORRECT_ID when the Profile with the given UUID as a String does not exist")
        void returnsIncorrectIdWhenTheProfileWithTheGivenUuidAsAStringDoesNotExist() {
            ServiceResponse<?> serviceResponse = profileService.getProfileById(UuidUtils.INVALID_UUID_AS_STRING);

            assertThat(serviceResponse).isEqualTo(INCORRECT_ID);
            verify(profileRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("Test if the createProfile() method")
    class CreateProfileMethodTest {
        @Test
        @DisplayName("returns INCORRECT_ID when the Account with the given UUID does not exist")
        void returnsIncorrectIdWhenTheAccountWithTheGivenUuidDoesNotExist()
                throws HttpMediaTypeNotSupportedException, IOException {
            when(accountRepository.findById(any())).thenReturn(Optional.empty());

            ServiceResponse<?> serviceResponse = profileService.createProfile(UUID.randomUUID(), testProfile, null);

            assertThat(serviceResponse).isEqualTo(INCORRECT_ID);
            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("returns ALREADY_EXISTS when the Account with the given UUID already has a Profile assigned")
        void returnsAlreadyExistsWhenTheAccountWithTheGivenUuidAlreadyHasAProfileAssigned()
                throws HttpMediaTypeNotSupportedException, IOException {
            Account account = new Account();
            account.setProfile(testProfile);
            when(accountRepository.findById(any())).thenReturn(Optional.of(account));

            ServiceResponse<?> serviceResponse = profileService.createProfile(UUID.randomUUID(), testProfile, null);

            assertThat(serviceResponse.getBody()).isEqualTo(ServiceResponseMessages.ACCOUNT_PROFILE_ALREADY_EXISTS);
            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("updates the Account with the given Profile")
        void updatesTheAccountWithTheGivenProfile() throws HttpMediaTypeNotSupportedException, IOException {
            Account account = new Account();
            account.setProfile(null);
            when(accountRepository.findById(any())).thenReturn(Optional.of(account));
            when(accountRepository.save(account)).thenReturn(account);

            profileService.createProfile(UUID.randomUUID(), testProfile, null);

            ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
            verify(accountRepository).save(accountArgumentCaptor.capture());
            assertThat(accountArgumentCaptor.getValue().getProfile()).isEqualTo(testProfile);
        }

        @Test
        @DisplayName("saves and returns the Profile when it has been assigned to the Account")
        void savesAndReturnsTheProfileWhenItHasBeenAssignedToTheAccount()
                throws HttpMediaTypeNotSupportedException, IOException {
            Account account = new Account();
            account.setProfile(null);
            when(accountRepository.findById(any())).thenReturn(Optional.of(account));
            when(accountRepository.save(account)).thenReturn(account);

            ServiceResponse<?> serviceResponse = profileService.createProfile(UUID.randomUUID(), testProfile, null);

            assertThat(serviceResponse.getBody()).isInstanceOf(Profile.class);
            verify(accountRepository).save(any());
        }
    }

    @Nested
    @DisplayName("Test if the updateProfile() method")
    class UpdateProfileTest {
        @Test
        @DisplayName("throws NullPointerException when the Profile update does not contain a UUID")
        void returnsIncorrectIdWhenTheProfileUpdateDoesNotContainAUuid() {
            testProfile.setId(null);

            assertThatThrownBy(() -> profileService.updateProfile(testProfile.getId(), testProfile))
                    .isExactlyInstanceOf(NullPointerException.class);
            verify(profileRepository, never()).findById(any());
        }

        @Test
        @DisplayName("returns INCORRECT_ID when the Profile with the given UUID does not exist")
        void returnsIncorrectIdWhenTheProfileWithTheGivenUuidDoesNotExist() {
            when(profileRepository.findById(any())).thenReturn(Optional.empty());

            ServiceResponse<?> serviceResponse = profileService.updateProfile(testProfile.getId(), testProfile);

            assertThat(serviceResponse).isEqualTo(INCORRECT_ID);
        }

        @Test
        @DisplayName("returns UNAUTHORIZED when the given Profile's UUID is different from the Profile's UUID of the " +
                     "requesting user")
        void returnsUnauthorizedWhenTheGivenProfileSUuidIsDifferentFromTheProfileSUuidOfTheRequestingUser() {
            when(profileRepository.findById(any())).thenReturn(Optional.of(testProfile));

            ServiceResponse<?> serviceResponse = profileService.updateProfile(UUID.randomUUID(), testProfile);

            assertThat(serviceResponse).isEqualTo(UNAUTHORIZED);
        }

        @Test
        @DisplayName("updates the Profile")
        void updatesTheProfile() {
            when(profileRepository.findById(any())).thenReturn(Optional.of(testProfile));
            // data preparation
            String sFirstName = "sTest";
            String sLastName = "sTest";
            DateFormatter birthDateFormatter = Profile.getBirthDateFormatter();
//            Date sBirthdate = birthDateFormatter.parse(birthDateFormatter.print(new Date(), Locale.getDefault()));
            Date sBirthdate = new GregorianCalendar(1999, Calendar.JUNE, 16).getTime();
            // test object set up
            Profile sProfile = new Profile(sFirstName, sLastName, sBirthdate);
            sProfile.setId(testProfile.getId());

            ServiceResponse<?> serviceResponse = profileService.updateProfile(testProfile.getId(), sProfile);

            ArgumentCaptor<Profile> profileArgumentCaptor = ArgumentCaptor.forClass(Profile.class);
            verify(profileRepository).save(profileArgumentCaptor.capture());
            assertThat(profileArgumentCaptor.getValue().getFirstName()).isEqualTo(sFirstName);
            assertThat(profileArgumentCaptor.getValue().getLastName()).isEqualTo(sLastName);
            assertThat(profileArgumentCaptor.getValue().getBirthDate()).isEqualTo(sBirthdate);
        }

        @Test
        @DisplayName("does not update the Profile with the given null values")
        void doesNotUpdateTheProfileWithTheGivenNullValues() {
            when(profileRepository.findById(any())).thenReturn(Optional.of(testProfile));
            // data preparation
            ProfileAvatar sAvatar = null;
            String sFirstName = null;
            String sLastName = null;
            Date sBirthdate = null;
            // test object set up
            Profile sProfile = new Profile(sFirstName, sLastName, sBirthdate);
            sProfile.setId(testProfile.getId());
            sProfile.setAvatar(sAvatar);

            ServiceResponse<?> serviceResponse = profileService.updateProfile(testProfile.getId(), sProfile);

            ArgumentCaptor<Profile> profileArgumentCaptor = ArgumentCaptor.forClass(Profile.class);
            verify(profileRepository).save(profileArgumentCaptor.capture());
            assertThat(profileArgumentCaptor.getValue().getAvatar()).isEqualTo(testProfile.getAvatar());
            assertThat(profileArgumentCaptor.getValue().getFirstName()).isEqualTo(testProfile.getFirstName());
            assertThat(profileArgumentCaptor.getValue().getLastName()).isEqualTo(testProfile.getLastName());
            assertThat(profileArgumentCaptor.getValue().getBirthDate()).isEqualTo(testProfile.getBirthDate());
        }

        @Test
        @DisplayName("saves and returns the Profile")
        void savesAndReturnsTheProfile() {
            when(profileRepository.findById(any())).thenReturn(Optional.of(testProfile));
            when(profileRepository.save(any())).thenReturn(testProfile);

            ServiceResponse<?> serviceResponse = profileService.updateProfile(testProfile.getId(), testProfile);

            assertThat(serviceResponse.getBody()).isEqualTo(testProfile);
        }
    }
}
