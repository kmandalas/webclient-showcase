package gr.kmandalas.service.appointment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerDTO {
    private String firstName;
    private String lastName;
    private Long accountId;
    private String email;
}
