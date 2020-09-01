package gr.kmandalas.service.appointment.controller;

import gr.kmandalas.service.appointment.dto.SendForm;
import gr.kmandalas.service.appointment.entity.OTP;
import gr.kmandalas.service.appointment.service.OTPService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/otp")
@RequiredArgsConstructor
public class OTPController {

  private final OTPService otpService;

  @GetMapping
  public Flux<OTP> getAll(@RequestParam(required = false) Long customerId) {
    return otpService.getAll(customerId);
  }

  @GetMapping("/{otpId}")
  public Mono<OTP> get(@PathVariable Long otpId) {
    return otpService.get(otpId);
  }

  @PostMapping
  public Mono<OTP> send(@RequestBody SendForm form) {
    return otpService.send(form);
  }

  @PostMapping("/{otpId}")
  public Mono<OTP> resend(@PathVariable Long otpId,
                          @RequestParam(required = false) Boolean sms,
                          @RequestParam(required = false) Boolean viber) {
    return otpService.resend(otpId, sms, viber);
  }

  @PostMapping("/validate/{otpId}")
  public Mono<String> validate(@PathVariable Long otpId, @RequestParam Integer pin) {
    return otpService.validate(otpId, pin);
  }
}
