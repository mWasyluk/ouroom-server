package pl.mwasyluk.ouroom_server.domain.converter;

import org.springframework.http.MediaType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MediaTypeConverter implements AttributeConverter<MediaType, String> {

    @Override
    public String convertToDatabaseColumn(MediaType mediaType) {
        return mediaType.getType() + '/' + mediaType.getSubtype();
    }

    @Override
    public MediaType convertToEntityAttribute(String s) {
        return MediaType.parseMediaType(s);
    }
}
