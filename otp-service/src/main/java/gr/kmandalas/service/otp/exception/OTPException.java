package gr.kmandalas.service.otp.exception;

import gr.kmandalas.service.otp.entity.OTP;
import gr.kmandalas.service.otp.enumeration.FaultReason;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class OTPException extends RuntimeException {

    public HttpStatus status;

    public FaultReason faultReason;

    public OTP otp;

    public OTPException(HttpStatus status, String message){
        super(message);
        this.status = status;
    }

	public OTPException(String message, FaultReason faultReason) {
		super(message);
		this.faultReason = faultReason;
	}

	public OTPException(String message, FaultReason faultReason, OTP otp) {
		super(message);
		this.faultReason = faultReason;
		this.otp = otp;
	}

}
