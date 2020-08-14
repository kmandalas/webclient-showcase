package gr.kmandalas.service.appointment.repository;

import gr.kmandalas.service.appointment.entity.Appointment;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AppointmentRepository extends ReactiveCrudRepository<Appointment, Long> {

}
