package com.project.back_end.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;

@Service
public class PrescriptionService {

	private final PrescriptionRepository prescriptionRepository;

	@Autowired
	public PrescriptionService(PrescriptionRepository prescriptionRepository) {
		this.prescriptionRepository = prescriptionRepository;
	}

	public ResponseEntity<?> savePrescription(Prescription prescription) {
		try {
			List<Prescription> existing = prescriptionRepository.findByAppointmentId(prescription.getAppointmentId());
			if (existing != null && !existing.isEmpty()) {
				return ResponseEntity.badRequest().body("Prescription already exists for this appointment");
			}
			prescriptionRepository.save(prescription);
			return ResponseEntity.status(201).body("Prescription created");
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Internal server error");
		}
	}

	public ResponseEntity<?> getPrescription(Long appointmentId) {
		try {
			List<Prescription> list = prescriptionRepository.findByAppointmentId(appointmentId);
			if (list == null || list.isEmpty()) {
				return ResponseEntity.ok().body(null);
			}
			return ResponseEntity.ok().body(list.get(0));
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Internal server error");
		}
	}

}
