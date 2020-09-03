package gr.kmandalas.service.otp.entity;

import gr.kmandalas.service.otp.enumeration.OTPStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("otp")
public class OTP {

	@Id
	@Column("id")
	private Long id;

	@Column("customer_id")
	private Long customerId;

	@Column("pin")
	private Integer pin;

	@Column("application_id")
	private Integer applicationId;

	@Column("attempt_count")
	private Integer attemptCount;

	@Column("created_on")
	private ZonedDateTime createdOn;

	@Column("status")
	private OTPStatus status;

}
