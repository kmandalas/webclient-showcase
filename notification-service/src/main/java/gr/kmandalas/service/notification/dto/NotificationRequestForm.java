package gr.kmandalas.service.notification.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NotificationRequestForm {
    @NotEmpty
    private String channel;
    private String msisdn;
    private String message;
}
