package gr.kmandalas.service.otp.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class OTPException extends RuntimeException {

    public HttpStatus status;

    public OTPException(HttpStatus status, String message){
        super(message);
        this.status = status;
    }

}
