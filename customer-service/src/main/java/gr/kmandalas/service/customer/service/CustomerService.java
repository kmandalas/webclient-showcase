package gr.kmandalas.service.customer.service;

import gr.kmandalas.service.customer.dto.CustomerDTO;
import gr.kmandalas.service.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    /**
     * Return customer info, by number
     * @param number the phone number of the customer
     * @return customer details
     */
    public Mono<CustomerDTO> findByNumber(String number) {
        return customerRepository.findByNumber(number)
                .map(customer -> CustomerDTO.builder()
                        .accountId(customer.getId())
                        .email(customer.getEmail())
                        .firstName(customer.getFirstName())
                        .lastName(customer.getLastName())
                        .build());
    }
}
