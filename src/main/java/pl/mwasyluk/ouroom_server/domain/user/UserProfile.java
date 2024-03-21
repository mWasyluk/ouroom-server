package pl.mwasyluk.ouroom_server.domain.user;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToOne;

import pl.mwasyluk.ouroom_server.domain.Presentable;
import pl.mwasyluk.ouroom_server.domain.media.Image;
import pl.mwasyluk.ouroom_server.exceptions.InitializationException;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Embeddable
public class UserProfile implements Presentable {
    @NonNull
    @Setter(AccessLevel.PROTECTED)
    private String firstname;
    @NonNull
    @Setter(AccessLevel.PROTECTED)
    private String lastname;

    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    private Image image;

    public UserProfile(ProfileBuilder profileBuilder) {
        this.firstname = profileBuilder.firstname.trim();
        this.lastname = profileBuilder.lastname.trim();
        this.image = profileBuilder.image;
    }

    @Override
    public String getName() {
        return firstname + " " + lastname;
    }

    public static class ProfileBuilder extends User.UserPropertiesBuilder {
        private String firstname;
        private String lastname;
        private Image image;

        protected ProfileBuilder(@NonNull User user) {
            super(user);
            if (target.profile != null) {
                firstname = target.profile.firstname;
                lastname = target.profile.lastname;
                image = target.profile.image;
            }
        }

        public ProfileBuilder setFirstname(String firstname) {
            this.firstname = firstname;
            return this;
        }

        public ProfileBuilder setLastname(String lastname) {
            this.lastname = lastname;
            return this;
        }

        public ProfileBuilder setImage(Image image) {
            this.image = image;
            return this;
        }

        protected void validate() {
            if (firstname == null || firstname.isBlank() || lastname == null || lastname.isBlank()) {
                throw new InitializationException("User names are invalid: '" + firstname + " " + lastname + "'");
            }
            target.profile = new UserProfile(this);
        }
    }
}
