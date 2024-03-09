package pl.mwasyluk.ouroom_server.services.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import pl.mwasyluk.ouroom_server.domain.media.Image;
import pl.mwasyluk.ouroom_server.domain.media.Media;
import pl.mwasyluk.ouroom_server.domain.media.source.DataSource;
import pl.mwasyluk.ouroom_server.domain.user.User;
import pl.mwasyluk.ouroom_server.domain.user.UserAccount;
import pl.mwasyluk.ouroom_server.domain.user.UserAuthority;
import pl.mwasyluk.ouroom_server.domain.user.UserProfile;
import pl.mwasyluk.ouroom_server.dto.user.UserDetailsForm;
import pl.mwasyluk.ouroom_server.dto.user.UserDetailsView;
import pl.mwasyluk.ouroom_server.dto.user.UserPresentableForm;
import pl.mwasyluk.ouroom_server.dto.user.UserPresentableView;
import pl.mwasyluk.ouroom_server.exceptions.ServiceException;
import pl.mwasyluk.ouroom_server.repos.UserRepository;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static pl.mwasyluk.ouroom_server.services.PrincipalValidator.validatePrincipal;
import static pl.mwasyluk.ouroom_server.utils.BcryptUtils.BCRYPT_PATTERN;
import static pl.mwasyluk.ouroom_server.utils.BcryptUtils.BCRYPT_PREFIX;

@RequiredArgsConstructor

@Service
public class DefaultUserService implements UserService, UserDetailsService {
    private final UserRepository userRepo;

    private void verifyPassword(String password) {
        if (!BCRYPT_PATTERN.matcher(password).matches()) {
            throw new ServiceException(UNPROCESSABLE_ENTITY,
                    "Requires password hashed via BCrypt algorithm and prefixed with '" + BCRYPT_PREFIX + "'.");
        }
    }

    @Override
    public @NonNull UserDetailsView create(@NonNull UserDetailsForm form) {
        // validation
        if (form.getEmail() == null || form.getPassword() == null) {
            throw new ServiceException(UNPROCESSABLE_ENTITY, "Email and password cannot be empty.");
        }

        // verification
        Optional<User> byEmail = userRepo.findByUsername(form.getEmail());
        if (byEmail.isPresent()) {
            throw new ServiceException(CONFLICT, "User with the given e-mail already exists.");
        }
        verifyPassword(form.getPassword());

        // execution
        User newUser = new User(form.getEmail(), form.getPassword());
        return new UserDetailsView(userRepo.save(newUser));
    }

    @Override
    public @NonNull UserDetailsView readDetails(@Nullable UUID userId) {
        // validation
        User principal = validatePrincipal();

        // verification
        User targetUser;
        if (userId != null) {
            if (!principal.getId().equals(userId) && !principal.getAuthorities().contains(UserAuthority.ADMIN)) {
                throw new ServiceException(FORBIDDEN, "Only owner user and admin can read account details.");
            }

            Optional<User> optionalUser = userRepo.findById(userId);
            if (optionalUser.isEmpty()) {
                throw new ServiceException(NOT_FOUND, "User with the given ID does not exist.");
            }
            targetUser = optionalUser.get();
        } else {
            targetUser = principal;
        }

        // execution
        return new UserDetailsView(targetUser);
    }

    @Override
    public @NonNull UserPresentableView readPresentable(@Nullable UUID userId) {
        // validation
        User principal = validatePrincipal();

        // verification
        User targetUser;
        if (userId != null) {
            Optional<User> optionalUser = userRepo.findById(userId);
            if (optionalUser.isEmpty()) {
                throw new ServiceException(NOT_FOUND, "User with the given ID does not exist.");
            }
            targetUser = optionalUser.get();
        } else {
            targetUser = principal;
        }

        // execution
        return new UserPresentableView(targetUser);
    }

    @Override
    public @NonNull UserPresentableView updateProfile(@NonNull UserPresentableForm form) {
        // validation
        User principal = validatePrincipal();
        if (principal.getName() == null && (form.getFirstname() == null || form.getLastname() == null)) {
            throw new ServiceException(UNPROCESSABLE_ENTITY,
                    "Firstname and lastname cannot be empty for a new profile.");
        }

        // verification
        UserProfile.ProfileBuilder profileBuilder = principal.profileBuilder();
        if (form.getFirstname() != null) {
            profileBuilder.setFirstname(form.getFirstname());
        }
        if (form.getLastname() != null) {
            profileBuilder.setLastname(form.getLastname());
        }
        if (form.isClearImage()) {
            profileBuilder.setImage(null);
        } else if (form.getFile() != null) {
            try {
                Media media = Media.of(DataSource.of(form.getFile().getBytes()));
                profileBuilder.setImage((Image) media);
            } catch (Exception e) {
                throw new ServiceException(UNPROCESSABLE_ENTITY, "The given file is not a valid image.");
            }
        }

        // execution
        return new UserPresentableView(userRepo.save(profileBuilder.apply()));
    }

    @Override
    public @NonNull UserDetailsView updateAccount(@NonNull UserDetailsForm form) {
        // validation
        User principal = validatePrincipal();

        // verification
        UserAccount.AccountBuilder accountBuilder = principal.accountBuilder();
        if (form.getEmail() != null) {
            if (!principal.getUsername().equals(form.getEmail())
                && userRepo.findByUsername(form.getEmail()).isPresent()) {
                throw new ServiceException(CONFLICT, "User with the given e-mail already exists.");
            }
            accountBuilder.setUsername(form.getEmail());
        }
        if (form.getPassword() != null) {
            verifyPassword(form.getPassword());
            accountBuilder.setPassword(form.getPassword());
        }

        // execution
        return new UserDetailsView(userRepo.save(accountBuilder.apply()));
    }

    @Override
    public void disable(@Nullable UUID userId) {
        // validation
        User principal = validatePrincipal();

        // verification
        User targetUser;
        if (userId != null && !principal.getId().equals(userId)) {
            if (!principal.getAuthorities().contains(UserAuthority.ADMIN)) {
                throw new ServiceException(FORBIDDEN, "User can only be deleted by themself or by an admin user.");
            }
            Optional<User> optionalUser = userRepo.findById(userId);
            if (optionalUser.isEmpty()) {
                throw new ServiceException(NOT_FOUND, "User with the given id does not exist.");
            }
            if (optionalUser.get().getAuthorities().contains(UserAuthority.ADMIN)) {
                throw new ServiceException(FORBIDDEN, "Admin user can only be deleted by themself.");
            }
            targetUser = optionalUser.get();
        } else {
            targetUser = principal;
        }

        // execution
        userRepo.save(targetUser.accountBuilder().setEnabled(false).apply());
    }

    @Override
    public User loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        // verification
        Optional<User> byEmail = userRepo.findByUsername(email);
        if (byEmail.isEmpty()) {
            throw new UsernameNotFoundException("User with email '" + email + "' could not be found");
        }

        // execution
        return byEmail.get();
    }
}
