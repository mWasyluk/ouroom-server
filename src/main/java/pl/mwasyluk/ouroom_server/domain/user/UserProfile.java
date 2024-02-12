package pl.mwasyluk.ouroom_server.domain.user;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
    @NonNull
    @Setter(AccessLevel.PROTECTED)
    private LocalDate birthDate;

    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    private Image image;

    public UserProfile(@NonNull String firstname, @NonNull String lastname, @NonNull LocalDate birthDate) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.birthDate = birthDate;
    }

    public long getAge() {
        return ChronoUnit.YEARS.between(birthDate, LocalDate.now());
    }

    @Override
    public String getName() {
        return firstname + " " + lastname;
    }
}
