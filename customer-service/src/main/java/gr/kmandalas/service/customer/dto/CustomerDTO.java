package gr.kmandalas.service.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {

    private String firstName;
    private String lastName;
    private Long accountId;
    private String email;

}
