package pl.mwasyluk.ouroom_server.newdomain.user;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import pl.mwasyluk.ouroom_server.newdomain.Identifiable;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor

@Entity
@Table(name = "users")
public class User extends Identifiable {
    @NonNull
    @Setter(AccessLevel.PROTECTED)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    protected UserAccount account;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    protected UserProfile profile;
}
