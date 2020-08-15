package gr.kmandalas.service.appointment.service;

import gr.kmandalas.service.appointment.entity.Appointment;
import gr.kmandalas.service.appointment.repository.AppointmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AppointmentService {

	@Autowired
	AppointmentRepository appointmentRepository;

	public Mono<Appointment> findById(Long appointmentId) {
		return appointmentRepository.findById(appointmentId);
	}

	public Flux<Appointment> findAll() {
		return appointmentRepository.findAll();
	}

	public Mono<Appointment> create(Appointment appointment) {
		log.info("About to create appointment: {}", appointment);
		return appointmentRepository.save(appointment);
	}

	public Mono<Appointment> update(Long appointmentId, Appointment appointment) {
		return appointmentRepository.save(appointment);
	}

	public Mono<Void> deleteById(Long appointmentId) {
		return appointmentRepository.deleteById(appointmentId);
	}

}
