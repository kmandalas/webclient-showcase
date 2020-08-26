package gr.kmandalas.service.appointment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;


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
  private String customerId;

  @Column("value")
  private Integer value;

  @Column("created_on")
  private LocalDateTime createdOn;

  @Column("status")
  private String status;

}
