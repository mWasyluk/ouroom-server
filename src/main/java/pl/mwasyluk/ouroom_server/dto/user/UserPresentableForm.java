package pl.mwasyluk.ouroom_server.dto.user;

import org.springframework.web.multipart.MultipartFile;
import lombok.Data;

@Data
public class UserPresentableForm {
    private String firstname;
    private String lastname;
    private MultipartFile file;
    private boolean clearImage;

    public String getFirstname() {
        return firstname == null || firstname.isBlank() ? null : firstname.trim();
    }

    public String getLastname() {
        return lastname == null || lastname.isBlank() ? null : lastname.trim();
    }
}
