package gr.kmandalas.service.appointment.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class NotificationRequestForm {
    @NotEmpty
    private String channel;
    private String msisdn;
    private String message;
}
