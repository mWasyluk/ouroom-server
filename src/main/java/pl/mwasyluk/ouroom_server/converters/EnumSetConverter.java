package pl.mwasyluk.ouroom_server.converters;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

import jakarta.persistence.AttributeConverter;

public abstract class EnumSetConverter<T extends Enum<T>> implements AttributeConverter<EnumSet<T>, String> {
    private static final String SEPARATOR = ",";

    abstract Class<T> clazz();

    @Override
    public String convertToDatabaseColumn(EnumSet<T> es) {
        if (es.isEmpty()) {
            return null;
        }

        return es.stream()
                .map(Enum::ordinal)
                .map(Object::toString)
                .collect(Collectors.joining(SEPARATOR));
    }

    @SuppressWarnings("unchecked")
    @Override
    public EnumSet<T> convertToEntityAttribute(String commaSeparatedEnums) {
        if (commaSeparatedEnums == null || commaSeparatedEnums.isEmpty()) {
            return EnumSet.noneOf(clazz());
        }

        String[] ordinalStrings = commaSeparatedEnums.split(SEPARATOR);

        if (ordinalStrings.length == 0) {
            return EnumSet.noneOf(clazz());
        }

        Enum<T>[] values = clazz().getEnumConstants();
        EnumSet<T> es = EnumSet.noneOf(clazz());

        Arrays.stream(ordinalStrings)
                .map(Integer::parseInt)
                .map(o -> values[o])
                .forEach(e -> es.add((T) e));

        return es;
    }
}
