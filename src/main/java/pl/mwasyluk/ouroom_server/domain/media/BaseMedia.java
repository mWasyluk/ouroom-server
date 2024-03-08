//package pl.mwasyluk.ouroom_server.domain.media;
//
//import org.springframework.http.MediaType;
//import lombok.AccessLevel;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//import lombok.NoArgsConstructor;
//import lombok.NonNull;
//import lombok.Setter;
//import lombok.extern.slf4j.Slf4j;
//import jakarta.persistence.Entity;
//import jakarta.persistence.Inheritance;
//import jakarta.persistence.InheritanceType;
//import jakarta.persistence.Table;
//
//import pl.mwasyluk.ouroom_server.domain.Identifiable;
//import pl.mwasyluk.ouroom_server.domain.media.source.DataSource;
//
//@Data
//@EqualsAndHashCode(onlyExplicitlyIncluded = true)
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@Slf4j
//
//@Entity
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
//@Table(name = "media")
//public abstract class BaseMedia extends Identifiable implements Media {
//
//    abstract protected void validate();
//
//    @Override
//    public @NonNull MediaType getType() {
//        return MediaType.parseMediaType(dataSource.getContentType());
//    }
//
//    @Override
//    public @NonNull DataSource getSource() {
//        return dataSource;
//    }
//}
