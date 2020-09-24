package gr.kmandalas.service.otp.controller;

import gr.kmandalas.service.otp.dto.SendForm;
import gr.kmandalas.service.otp.entity.OTP;
import gr.kmandalas.service.otp.service.OTPService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/v1/otp")
@RequiredArgsConstructor
public class OTPController {

	private final OTPService otpService;

	@PostMapping
	public Mono<OTP> send(@RequestBody SendForm form) {
		return otpService.send(form);
	}

	@PostMapping("/{otpId}")
	public Mono<OTP> resend(@PathVariable Long otpId, @RequestParam(required = false) List<String> via,
			@RequestParam(required = false) String mail) {
		return otpService.resend(otpId, via, mail);
	}

	@PostMapping("/{otpId}/validate")
	public Mono<OTP> validate(@PathVariable Long otpId, @RequestParam Integer pin) {
		return otpService.validate(otpId, pin);
	}

	@GetMapping
	public Flux<OTP> getAll(@RequestParam(required = false) String number) {
		return otpService.getAll(number);
	}

	@GetMapping("/{otpId}")
	public Mono<OTP> get(@PathVariable Long otpId) {
		return otpService.get(otpId);
	}

}
