package gr.kmandalas.service.customer.controller;

import gr.kmandalas.service.customer.dto.CustomerDTO;
import gr.kmandalas.service.customer.exception.CustomerNotFoundException;
import gr.kmandalas.service.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public Mono<ResponseEntity<CustomerDTO>> getCustomer(@RequestParam String number) {
        return customerService.findByNumber(number).map(ResponseEntity::ok);
    }

    @ExceptionHandler
    public ResponseEntity<Void> handleCustomerNotFoundException(CustomerNotFoundException ex){
        log.warn("Customer not found: ", ex);
        return ResponseEntity.notFound().build();
    }

}
