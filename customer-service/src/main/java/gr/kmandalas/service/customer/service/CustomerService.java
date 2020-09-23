package gr.kmandalas.service.customer.service;

import gr.kmandalas.service.customer.dto.CustomerDTO;
import gr.kmandalas.service.customer.dto.CustomerForm;
import gr.kmandalas.service.customer.entity.Customer;
import gr.kmandalas.service.customer.exception.CustomerNotFoundException;
import gr.kmandalas.service.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Return customer info, by number
     * @param number the phone number of the customer
     * @return customer details
     */
    @NewSpan
    public Mono<CustomerDTO> findByNumber(String number) {
        return customerRepository.findByNumber(number)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException("Customer with number: " + number + " not found")))
                .map(customer -> CustomerDTO.builder()
                        .accountId(customer.getId())
                        .email(customer.getEmail())
                        .firstName(customer.getFirstName())
                        .lastName(customer.getLastName())
                        .build());
    }


    /**
     * Create new customer in database
     * @param customerForm the form containing the customer's details
     * @return Customer entity mono
     */
    @NewSpan
    public Mono<CustomerDTO> insertCustomer(CustomerForm customerForm) {
        return customerRepository.save(Customer.builder()
                .email(customerForm.getEmail())
                .createdAt(LocalDateTime.now())
                .firstName(customerForm.getFirstName())
                .lastName(customerForm.getLastName())
                .number(customerForm.getNumber())
                .build())
                .map(customer -> CustomerDTO.builder()
                        .lastName(customer.getLastName())
                        .firstName(customer.getFirstName())
                        .email(customer.getEmail())
                        .accountId(customer.getId())
                        .build());
    }

}
