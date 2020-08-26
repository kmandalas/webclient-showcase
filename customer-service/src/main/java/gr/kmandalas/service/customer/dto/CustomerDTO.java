package gr.kmandalas.service.customer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerDTO {
    private String firstName;
    private String lastName;
    private String accountId;
    private String email;
}
