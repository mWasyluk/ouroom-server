package pl.mwasyluk.ouroom_server.converters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import pl.mwasyluk.ouroom_server.domain.media.source.DataSource;
import pl.mwasyluk.ouroom_server.domain.media.source.ExternalDataSource;
import pl.mwasyluk.ouroom_server.domain.media.source.InternalDataSource;

@Converter(autoApply = true)
public class DataSourceColumnConverter implements AttributeConverter<DataSource, byte[]> {
    private static final byte INTERNAL_CODE = 0x00;
    private static final byte EXTERNAL_CODE = 0x01;

    @Override
    public byte[] convertToDatabaseColumn(DataSource dataSource) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        if (dataSource instanceof InternalDataSource ids) {
            try {
                os.write(INTERNAL_CODE);
                ids.getInputStream().transferTo(os);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (dataSource instanceof ExternalDataSource eds) {
            try {
                os.write(EXTERNAL_CODE);
                os.write(eds.getUrl().toString().getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return os.toByteArray();
    }

    @Override
    public DataSource convertToEntityAttribute(byte[] bytes) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        if (bytes[0] == INTERNAL_CODE) {
            os.write(bytes, 1, bytes.length - 1);
            return DataSource.of(os.toByteArray());
        } else if (bytes[0] == EXTERNAL_CODE) {
            os.write(bytes, 1, bytes.length - 1);
            try {
                return DataSource.of(os.toString());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
