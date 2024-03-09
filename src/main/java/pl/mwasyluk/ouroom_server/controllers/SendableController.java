package pl.mwasyluk.ouroom_server.controllers;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import pl.mwasyluk.ouroom_server.domain.media.Media;
import pl.mwasyluk.ouroom_server.dto.sendable.SendableForm;
import pl.mwasyluk.ouroom_server.dto.sendable.SendableView;
import pl.mwasyluk.ouroom_server.services.media.MediaService;
import pl.mwasyluk.ouroom_server.services.sendable.SendableService;

@Tag(name = "Sendable API")
@RequiredArgsConstructor

@RestController
@RequestMapping(value = "${server.api.prefix}/sendables")
public class SendableController {
    private final SendableService sendableService;
    private final MediaService mediaService;

    @Operation(summary = "Get all sendables by container ID")
    @GetMapping
    public ResponseEntity<Collection<SendableView>> readAllByContainerId(
            @RequestParam UUID containerId
    ) {
        return ResponseEntity.ok(sendableService.readAllFromContainer(containerId));
    }

    @Operation(summary = "Create new sendable",
               description = "Persists sent files and appends their URLs to sendable's content.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SendableView> create(
            @RequestParam UUID containerId,
            @RequestParam String message,
            @RequestParam(required = false) List<MultipartFile> file
    ) {
        SendableForm form = new SendableForm();
        form.setContainerId(containerId);

        String finalMessage = message == null || message.isBlank() ? "" : message + "\n";
        if (file != null && file.size() > 0) {
            StringBuilder textBuilder = new StringBuilder(finalMessage);
            for (MultipartFile f : file) {
                Media targetMedia = mediaService.create(f);
                textBuilder.append(targetMedia.getInternalUrl()).append("\n");
            }
            finalMessage = textBuilder.toString();
        }
        form.setMessage(finalMessage);

        return ResponseEntity.ok(sendableService.create(form));
    }

    @Operation(summary = "Update sendable by ID")
    @PatchMapping
    public ResponseEntity<SendableView> update(
            @RequestParam UUID sendableId,
            @RequestParam String message
    ) {
        SendableForm form = new SendableForm();
        form.setSendableId(sendableId);
        form.setMessage(message);

        return ResponseEntity.ok(sendableService.update(form));
    }

    @Operation(summary = "Delete sendable by ID")
    @DeleteMapping
    public ResponseEntity<?> delete(
            @RequestParam UUID sendableId
    ) {
        sendableService.delete(sendableId);
        return ResponseEntity.ok().build();
    }
}
