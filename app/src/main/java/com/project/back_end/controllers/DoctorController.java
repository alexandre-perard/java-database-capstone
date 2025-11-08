package com.project.back_end.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
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

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {

	private final DoctorService doctorService;
	private final Service service;

	@Autowired
	public DoctorController(DoctorService doctorService, Service service) {
		this.doctorService = doctorService;
		this.service = service;
	}

	@GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
	public ResponseEntity<?> getDoctorAvailability(@PathVariable String user, @PathVariable Long doctorId,
			@PathVariable String date, @PathVariable String token) {
		if (!service.validateToken(token, user)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
		}
		LocalDateTime dt;
		try {
			if (date.contains("T")) dt = LocalDateTime.parse(date);
			else dt = LocalDate.parse(date).atStartOfDay();
		} catch (DateTimeParseException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid date format"));
		}
		List<String> availability = doctorService.getDoctorAvailability(doctorId, dt);
		return ResponseEntity.ok(Map.of("availability", availability));
	}

	@GetMapping("")
	public ResponseEntity<?> getDoctors() {
		List<Doctor> docs = doctorService.getDoctors();
		return ResponseEntity.ok(Map.of("doctors", docs));
	}

	@PostMapping("/save/{token}")
	public ResponseEntity<?> saveDoctor(@RequestBody Doctor doctor, @PathVariable String token) {
		if (!service.validateToken(token, "admin")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
		}
		int r = doctorService.saveDoctor(doctor, token);
		if (r == -1) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Doctor exists"));
		if (r == 1) return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("status", "created"));
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Could not save"));
	}

	@PostMapping("/login")
	public ResponseEntity<?> doctorLogin(@RequestBody Login login) {
		if (login == null || login.getEmail() == null) return ResponseEntity.badRequest().body(Map.of("error", "Missing creds"));
		String token = doctorService.validateDoctor(login.getEmail(), login.getPassword());
		if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
		return ResponseEntity.ok(Map.of("token", token));
	}

	@PutMapping("/update/{token}")
	public ResponseEntity<?> updateDoctor(@RequestBody Doctor doctor, @PathVariable String token) {
		if (!service.validateToken(token, "admin")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","Invalid token"));
		int r = doctorService.updateDoctor(doctor);
		if (r == -1) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error","Doctor not found"));
		if (r == 1) return ResponseEntity.ok(Map.of("status","updated"));
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error","Could not update"));
	}

	@DeleteMapping("/delete/{id}/{token}")
	public ResponseEntity<?> deleteDoctor(@PathVariable Long id, @PathVariable String token) {
		if (!service.validateToken(token, "admin")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","Invalid token"));
		int r = doctorService.deleteDoctor(id);
		if (r == -1) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error","Not found"));
		if (r == 1) return ResponseEntity.ok(Map.of("status","deleted"));
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error","Could not delete"));
	}

	@GetMapping("/filter/{name}/{time}/{speciality}")
	public ResponseEntity<?> filter(@PathVariable String name, @PathVariable String time, @PathVariable String speciality) {
		List<Doctor> res = service.filterDoctor(name, time, speciality);
		return ResponseEntity.ok(Map.of("doctors", res));
	}

}
