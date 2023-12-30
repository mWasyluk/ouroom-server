package pl.mwasyluk.ouroom_server.domain.message;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sun.istack.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "messages")
public class Message implements Comparable<Message> {
    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne(optional = false)
    @JsonIgnoreProperties({"participators"})
    private Conversation conversation;

    private UUID sourceUserId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date sentDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date deliveryDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date readDate;

    @NotNull
    private String content;

    public Message(UUID sourceUserId, TempMessage tempMessage) {
        this.sourceUserId = sourceUserId;
        this.content = tempMessage.getContent();
    }

    public MessageState getMessageState() {
        return sentDate == null ? MessageState.NOT_SENT
                : deliveryDate == null ? MessageState.NOT_DELIVERED
                : readDate == null ? MessageState.NOT_READ : MessageState.READ;
    }

    @Override
    public int compareTo(Message o) {
        return o.getSentDate().compareTo(this.getSentDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sourceUserId, content);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Message message = (Message) o;
        return Objects.equals(id, message.id) && Objects.equals(sourceUserId, message.sourceUserId)
               && Objects.equals(content, message.content);
    }

    @Override
    public String toString() {
        return "Message{" +
               "id=" + id +
               ", sourceUserId=" + sourceUserId +
               ", conversation=" + conversation +
               ", sentDate=" + sentDate +
               ", deliveryDate=" + deliveryDate +
               ", readDate=" + readDate +
               ", content='" + content + '\'' +
               '}';
    }

    @Data
    @NoArgsConstructor
    public static class TempMessage {
        private String content;

        public TempMessage(String content) {
            this.content = content;
        }
    }

    // TODO: DTO for messages with participators with only id field
}
