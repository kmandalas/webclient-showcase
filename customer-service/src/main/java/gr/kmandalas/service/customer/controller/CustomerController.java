package gr.kmandalas.service.customer.controller;

import gr.kmandalas.service.customer.dto.CustomerDTO;
import gr.kmandalas.service.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public Mono<ResponseEntity<CustomerDTO>> getCustomer(@RequestParam String number) {
        return customerService.findByNumber(number).map(ResponseEntity::ok);
    }

}
