package gr.kmandalas.service.notification.controller;

import gr.kmandalas.service.notification.dto.NotificationRequestForm;
import gr.kmandalas.service.notification.dto.NotificationResultDTO;
import gr.kmandalas.service.notification.enums.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    @PostMapping
    public ResponseEntity<NotificationResultDTO> sendNotification(@RequestBody @Valid NotificationRequestForm form) {
        try {
            Channel notificationMethod = Channel.valueOf(form.getChannel());

			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}

            return ResponseEntity.ok()
                    .body(NotificationResultDTO.builder()
                            .status("OK")
                            .message("Notification sent to " + notificationMethod.name())
                            .build());

        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .badRequest()
                    .body(NotificationResultDTO.builder()
                            .status("ERROR")
                            .message("Unsupported communication channel")
                            .build());
        }
    }

}
