package gr.kmandalas.service.appointment.entity;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ToString
public class Appointment {

    @Id
	private Long id;
    private LocalDateTime createdAt;
	private AppointmentStatus status = AppointmentStatus.Booked;
	private LocalDate appointmentDate;
    private LocalDateTime appointmentStartTime;
    private LocalDateTime appointmentEndTime;
	private String customerEmail;
	private String notificationPhoneNumber;
	private String serviceAddress;

}
