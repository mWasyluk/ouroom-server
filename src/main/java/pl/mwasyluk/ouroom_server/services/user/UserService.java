package pl.mwasyluk.ouroom_server.services.user;

import java.util.UUID;

import org.springframework.lang.Nullable;
import lombok.NonNull;

import pl.mwasyluk.ouroom_server.dto.user.UserDetailsForm;
import pl.mwasyluk.ouroom_server.dto.user.UserDetailsView;
import pl.mwasyluk.ouroom_server.dto.user.UserPresentableForm;
import pl.mwasyluk.ouroom_server.dto.user.UserPresentableView;

public interface UserService {
    @NonNull UserDetailsView create(@NonNull UserDetailsForm userDetailsForm);

    @NonNull UserDetailsView readDetails(@Nullable UUID userId);
    @NonNull UserPresentableView readPresentable(@Nullable UUID userId);

    @NonNull UserPresentableView updateProfile(@NonNull UserPresentableForm userPresentableForm);
    @NonNull UserDetailsView updateAccount(@NonNull UserDetailsForm userDetailsForm);

    void disable(@Nullable UUID userId);
}
