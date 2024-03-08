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
        this.firstname = profileBuilder.firstname;
        this.lastname = profileBuilder.lastname;
        this.image = profileBuilder.image;
    }

    @Override
    public String getName() {
        return firstname + " " + lastname;
    }
}
