package com.project.back_end.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;
import com.project.back_end.services.TokenService;

@RestController
@RequestMapping("/patient")
public class PatientController {

	private final PatientService patientService;
	private final Service service;
	private final TokenService tokenService;

	@Autowired
	public PatientController(PatientService patientService, Service service, TokenService tokenService) {
		this.patientService = patientService;
		this.service = service;
		this.tokenService = tokenService;
	}

	@GetMapping("/get/{token}")
	public ResponseEntity<?> getPatient(@PathVariable String token) {
		if (!service.validateToken(token, "patient")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
		}
		Patient p = patientService.getPatientDetails(token);
		if (p == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Patient not found"));
		return ResponseEntity.ok(p);
	}

	@PostMapping("/create")
	public ResponseEntity<?> createPatient(@RequestBody Patient patient) {
		if (patient == null || patient.getEmail() == null) return ResponseEntity.badRequest().body(Map.of("error","Missing data"));
		boolean ok = service.validatePatient(patient.getEmail(), patient.getPhone());
		if (!ok) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error","Patient already exists"));
		int r = patientService.createPatient(patient);
		if (r == 1) return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("status","created"));
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error","Could not create patient"));
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody Login login) {
		if (login == null || login.getEmail() == null) return ResponseEntity.badRequest().body(Map.of("error","Missing creds"));
		Patient p = service.validatePatientLogin(login.getEmail(), login.getPassword());
		if (p == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","Invalid credentials"));
		String token = tokenService.generateToken(p.getEmail());
		return ResponseEntity.ok(Map.of("token", token));
	}

	@GetMapping("/appointments/{patientId}/{token}/{user}")
	public ResponseEntity<?> getPatientAppointment(@PathVariable Long patientId, @PathVariable String token, @PathVariable String user) {
		if (!service.validateToken(token, user)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","Invalid token"));
		return ResponseEntity.ok(patientService.getPatientAppointments(patientId));
	}

	@GetMapping("/filterAppointments/{condition}/{name}/{token}")
	public ResponseEntity<?> filterPatientAppointment(@PathVariable String condition, @PathVariable String name, @PathVariable String token) {
		if (!service.validateToken(token, "patient")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","Invalid token"));
		return ResponseEntity.ok(service.filterPatient(token, condition, name));
	}

}


