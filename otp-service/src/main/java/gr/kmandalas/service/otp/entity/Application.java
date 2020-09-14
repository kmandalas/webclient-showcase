package gr.kmandalas.service.otp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("application")
public class Application {

	@Id
	@Column("app_key")
	private String uuid;

	@Column("attempts_allowed")
	private Integer attemptsAllowed;

	@Column("name")
	private String name;

	@Column("ttl")
	private Integer ttl;

	@Column("is_default")
	private Boolean isDefault;

}
