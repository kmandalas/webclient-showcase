package gr.kmandalas.service.appointment.controller;

import gr.kmandalas.service.appointment.entity.OTP;
import gr.kmandalas.service.appointment.service.OTPService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/otp")
@RequiredArgsConstructor
public class OTPController {

  private final OTPService otpService;

  @GetMapping
  public Mono<OTP> get(@RequestParam String customerId) {
    return otpService.get(customerId);
  }

  @PostMapping
  public Mono<OTP> generate(@RequestParam String customerId) {
    return otpService.generate(customerId);
  }

  @PostMapping("/{providedOTP}")
  public Mono<String> validate(@PathVariable Integer providedOTP,
                               @RequestParam String customerId) {
    return otpService.validate(providedOTP, customerId);
  }
}
