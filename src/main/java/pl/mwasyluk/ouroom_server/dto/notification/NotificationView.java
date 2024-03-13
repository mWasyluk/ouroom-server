package pl.mwasyluk.ouroom_server.dto.notification;

import lombok.NonNull;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Transient;

import pl.mwasyluk.ouroom_server.exceptions.ConversionException;

public record NotificationView(
        @NonNull String action,
        @NonNull String content
) {
    @Transient
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static String mapToString(Object o) {
        try {
            objectMapper.findAndRegisterModules();
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new ConversionException("The given Object could not be mapped to string.");
        }
    }

    public NotificationView(@NonNull Action action, @NonNull Object view) {
        this(action.toString(), mapToString(view));
    }

    public enum Action {
        NEW, CHANGED, REMOVED
    }
}
