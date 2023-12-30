package pl.mwasyluk.ouroom_server.domain.userdetails;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.persistence.*;

import org.springframework.format.datetime.DateFormatter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;

import pl.mwasyluk.ouroom_server.domain.message.Conversation;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor

@Entity
@Table(name = "profiles")
public class Profile {
    private static DateFormatter dateFormatter = new DateFormatter("yyyy-MM-dd");
    @Id
    private UUID id = UUID.randomUUID();
    private String firstName;
    private String lastName;
    @OneToOne(mappedBy = "profile")
    @JsonIgnore
    private Account account;
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "participators_conversations")
    @JsonIgnore
    private List<Conversation> conversations = new ArrayList<>();
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "avatar_id", referencedColumnName = "id")
    private ProfileAvatar avatar = null;
    @Temporal(TemporalType.DATE)
    @JsonDeserialize(using = DateDeserializers.CalendarDeserializer.class)
    private Date birthDate;

    public static DateFormatter getBirthDateFormatter() {
        return dateFormatter;
    }

    public Profile(String firstName, String lastName, Date birthDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
    }

    public void addConversation(Conversation conversation) {
        this.conversations.add(conversation);
    }

    public void removeConversation(Conversation conversation) {
        this.conversations.removeIf(conv -> conv.getId().equals(conversation.getId()));
    }

    public ProfileAvatar getAvatar() {
        return avatar;
    }

    @JsonProperty("birthdate")
    public String getBirthDateAsString() {
        if (birthDate != null) {
            return dateFormatter.print(birthDate, Locale.getDefault());
        }
        return "";
    }

    @JsonIgnore
    public Date getBirthDate() {
        return this.birthDate;
    }

    @JsonDeserialize()
    public void setBirthDate(String ddmmyyyy) {
        String[] split = ddmmyyyy.split("-");
        try {
            this.birthDate = dateFormatter.parse(split[0] + "-" + split[1] + "-" + split[2], Locale.getDefault());
        } catch (ParseException e) {
            System.err.println("Parsing string to date failed. String: " + ddmmyyyy);
        }
    }
}
