package pl.mwasyluk.ouroom_server.domain.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Data;
import lombok.NonNull;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import pl.mwasyluk.ouroom_server.domain.userdetails.Profile;

@Data

@Entity
@Table(name = "conversations")
public class Conversation {
    @Id
    private UUID id = UUID.randomUUID();
    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @ElementCollection(targetClass = Message.class)
    @JoinTable(name = "conversation_messages")
    @JsonIgnore
    private List<Message> messages;

    @ManyToMany
    @JsonIgnoreProperties({"birthDate"})
    private List<Profile> participators;

    public Conversation() {
        this.messages = new ArrayList<>();
        this.participators = new ArrayList<>();
    }

    public Conversation(List<Profile> participators) {
        this.messages = new ArrayList<>();
        this.participators = new ArrayList<>(participators);
    }

    public void addParticipators(@NonNull Collection<Profile> profiles) {
        profiles.forEach(profile -> profile.addConversation(this));
        this.participators.addAll(profiles);
    }

    public boolean addMessage(Message message) {
        if (messages.contains(message)) {
            return false;
        }
        message.setConversation(this);
        return this.messages.add(message);
    }

    public void removeMessageById(UUID messageId) {
        Optional<Message> matchingMessage = this.messages.stream()
                .filter(message -> message.getId().equals(messageId))
                .findAny();
        matchingMessage.ifPresent(messages::remove);
    }

    public Set<Message> getLatestMessages(int quantity) {
        Iterator<Message> iterator = this.messages.iterator();
        List<Message> latest = new LinkedList<>();

        while (iterator.hasNext() && latest.size() < quantity) {
            latest.add(iterator.next());
        }

        return new TreeSet<>(latest);
    }
    //
    // public void setParticipators(Set<Profile> participators){
    // this.participators = new ArrayList<>(participators);
    // }

    // TODO: remove, add -Participator : Profile; removeAll, addAll -Participators : List<Profile>
}
