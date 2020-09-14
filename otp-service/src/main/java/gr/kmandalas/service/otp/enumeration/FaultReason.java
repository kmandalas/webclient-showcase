package gr.kmandalas.service.otp.enumeration;

public enum FaultReason {

	EXPIRED("OTP has expired"),
	TOO_MANY_ATTEMPTS("Too many validation attempts"),
	INVALID_PIN("Wrong PIN"),
	INVALID_STATUS("Invalid status"),
	NOT_FOUND("Resource not found"),
	CUSTOMER_ERROR("Customer retrieval failed"),
	NUMBER_INFORMATION_ERROR("MSIDN status check failed"),
	GENERIC_ERROR("The server was unable to fulfill the request");

	private final String message;

	FaultReason(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
