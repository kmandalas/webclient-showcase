package gr.kmandalas.service.otp.advice;

import gr.kmandalas.service.otp.enumeration.FaultReason;
import gr.kmandalas.service.otp.exception.OTPException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class OTPControllerAdvice {

    @ExceptionHandler(OTPException.class)
    public ResponseEntity<String> handleOTPException(OTPException ex) {
		final FaultReason faultReason = ex.getFaultReason() != null ? ex.getFaultReason() : FaultReason.GENERIC_ERROR;
		log.error("otp-service error", ex);

		HttpStatus httpStatus;
		switch (faultReason) {
			case NOT_FOUND:
				httpStatus = HttpStatus.NOT_FOUND;
				break;
			case TOO_MANY_ATTEMPTS: {
				httpStatus = HttpStatus.BAD_REQUEST;
				break;
			}
			case EXPIRED:
			case INVALID_PIN:
			case INVALID_STATUS:
			case CUSTOMER_ERROR: {
				httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
				break;
			}
			case NUMBER_INFORMATION_ERROR:
			default: {
				httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			}
		}

		return ResponseEntity.status(httpStatus).body(faultReason.getMessage());
    }

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleException(Exception ex) {
		log.error(ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server was unable to fulfill the request");
	}

}
