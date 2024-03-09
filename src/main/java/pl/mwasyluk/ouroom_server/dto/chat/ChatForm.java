package pl.mwasyluk.ouroom_server.dto.chat;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;
import lombok.Data;

@Data
public class ChatForm {
    private UUID chatId;
    private String name;
    private boolean clearImage;
    private MultipartFile file;

    public String getName() {
        return name == null || name.isBlank() ? null : name.trim();
    }
}
