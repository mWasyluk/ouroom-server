package pl.mwasyluk.ouroom_server.newdomain.user;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import pl.mwasyluk.ouroom_server.newdomain.Presentable;
import pl.mwasyluk.ouroom_server.newdomain.media.Image;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
@Table(name = "user_profiles")
public class UserProfile extends Presentable {
    @NonNull
    @Setter(AccessLevel.PROTECTED)
    private String firstName;
    @NonNull
    @Setter(AccessLevel.PROTECTED)
    private String lastName;
    @NonNull
    @Setter(AccessLevel.PROTECTED)
    private LocalDate birthDate;

    public UserProfile(@NonNull String firstName, @NonNull String lastName, @NonNull LocalDate birthDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
    }

    public void setImage(Image image) {
        super.image = image;
    }

    public long getAge() {
        return ChronoUnit.YEARS.between(birthDate, LocalDate.now());
    }

    @Override
    public String getName() {
        return firstName + " " + lastName;
    }
}
