package gr.kmandalas.service.otp.advice;

import gr.kmandalas.service.otp.exception.OTPException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class OTPControllerAdvice {

    @ExceptionHandler(OTPException.class)
    public ResponseEntity<String> handleOTPException(OTPException ex){
        return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
    }
}
