package gr.kmandalas.service.appointment.controller;

import gr.kmandalas.service.appointment.entity.Appointment;
import gr.kmandalas.service.appointment.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

	/** GET request to return all appointments **/
	@GetMapping
	Flux<Appointment> findAll() {
		return appointmentService.findAll();
	}

    /** GET request to return specific appointments **/
    @GetMapping("/{appointmentId}")
    public Mono<Appointment> findById(@PathVariable Long appointmentId) {
        return appointmentService.findById(appointmentId);
    }

    /** POST request to add new appointments **/
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Appointment> create(@RequestBody Appointment appointment) {
        return appointmentService.create(appointment);
    }

    /** PUT request to update appointments **/
    @PutMapping("/{appointmentId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Appointment> update(@PathVariable Long appointmentId, @RequestBody Appointment appointment) {
        return appointmentService.update(appointmentId, appointment);
    }

    /** DELETE request to delete specific appointments **/
    @DeleteMapping("/{appointmentId}")
    @ResponseStatus(HttpStatus.OK)
    Mono<Void> deleteById(@PathVariable Long appointmentId) {
        return appointmentService.deleteById(appointmentId);
    }

}
