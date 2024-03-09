package pl.mwasyluk.ouroom_server.controllers;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import pl.mwasyluk.ouroom_server.domain.media.Media;
import pl.mwasyluk.ouroom_server.domain.media.source.ExternalDataSource;
import pl.mwasyluk.ouroom_server.services.media.MediaService;

@Tag(name = "Media API")
@RequiredArgsConstructor

@RestController
@RequestMapping("${server.api.prefix}/media")
public class MediaController {
    private final MediaService mediaService;

    @Operation(summary = "Get media content or redirection by ID")
    @GetMapping(value = "/{mediaId}")
    public ResponseEntity<?> readById(@PathVariable UUID mediaId) {
        Media targetMedia = mediaService.read(mediaId);
        if (targetMedia.getSource() instanceof ExternalDataSource eds) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", eds.getUrl().toString());
            return new ResponseEntity<>(headers, HttpStatus.PERMANENT_REDIRECT);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(targetMedia.getType());
        return new ResponseEntity<>(targetMedia.getSource().getData(), headers, HttpStatus.OK);
    }
}
