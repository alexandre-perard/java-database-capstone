package com.project.back_end.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;

@RestController
@RequestMapping("${api.path}prescription")
public class PrescriptionController {

	private final PrescriptionService prescriptionService;
	private final Service service;
	private final AppointmentService appointmentService;

	@Autowired
	public PrescriptionController(PrescriptionService prescriptionService, Service service, AppointmentService appointmentService) {
		this.prescriptionService = prescriptionService;
		this.service = service;
		this.appointmentService = appointmentService;
	}

	@PostMapping("/save/{token}")
	public ResponseEntity<?> savePrescription(@RequestBody Prescription prescription, @PathVariable String token) {
		if (!service.validateToken(token, "doctor")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of("error", "Invalid token"));
		}
		ResponseEntity<?> resp = prescriptionService.savePrescription(prescription);
		if (resp.getStatusCode().is2xxSuccessful()) {
			try {
				Long appId = prescription.getAppointmentId();
				if (appId != null) appointmentService.changeStatus(appId, 2); // 2 denotes prescription-created
			} catch (Exception e) {
				// ignore status update failure
			}
		}
		return resp;
	}

	@GetMapping("/get/{appointmentId}/{token}")
	public ResponseEntity<?> getPrescription(@PathVariable Long appointmentId, @PathVariable String token) {
		if (!service.validateToken(token, "doctor")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of("error", "Invalid token"));
		}
		return prescriptionService.getPrescription(appointmentId);
	}

}
