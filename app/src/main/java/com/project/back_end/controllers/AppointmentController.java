package com.project.back_end.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

	private final AppointmentService appointmentService;
	private final Service service;
	private final PatientService patientService;

	@Autowired
	public AppointmentController(AppointmentService appointmentService, Service service, PatientService patientService) {
		this.appointmentService = appointmentService;
		this.service = service;
		this.patientService = patientService;
	}

	@GetMapping("/{date}/{patientName}/{token}")
	public ResponseEntity<?> getAppointments(@PathVariable String date, @PathVariable String patientName,
			@PathVariable String token) {
		if (!service.validateToken(token, "doctor")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
		}
		LocalDateTime dt;
		try {
			if (date.contains("T")) {
				dt = LocalDateTime.parse(date);
			} else {
				dt = LocalDate.parse(date).atStartOfDay();
			}
		} catch (DateTimeParseException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid date format"));
		}
		return ResponseEntity.ok(appointmentService.getAllAppointments(dt, patientName, token));
	}

	@PostMapping("/book/{token}")
	public ResponseEntity<?> bookAppointment(@RequestBody Appointment appointment, @PathVariable String token) {
		if (!service.validateToken(token, "patient")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
		}
		if (appointment == null || appointment.getDoctor() == null || appointment.getAppointmentTime() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Missing appointment data"));
		}
		Long doctorId = appointment.getDoctor().getId();
		int valid = appointmentService.validateAppointment(doctorId, appointment.getAppointmentTime());
		if (valid == -1) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Doctor not found"));
		if (valid == 0) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Slot already taken"));
		int res = appointmentService.bookAppointment(appointment);
		if (res == 1) return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("status", "booked"));
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Could not book appointment"));
	}

	@PutMapping("/update/{token}")
	public ResponseEntity<?> updateAppointment(@RequestBody Appointment appointment, @PathVariable String token) {
		if (!service.validateToken(token, "patient")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
		}
		boolean ok = appointmentService.updateAppointment(appointment);
		if (ok) return ResponseEntity.ok(Map.of("status", "updated"));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Unable to update"));
	}

	@DeleteMapping("/cancel/{appointmentId}/{token}")
	public ResponseEntity<?> cancelAppointment(@PathVariable Long appointmentId, @PathVariable String token) {
		if (!service.validateToken(token, "patient")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
		}
		Patient p = patientService.getPatientDetails(token);
		if (p == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
		boolean ok = appointmentService.cancelAppointment(appointmentId, p.getId());
		if (ok) return ResponseEntity.ok(Map.of("status", "cancelled"));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Unable to cancel"));
	}

}
