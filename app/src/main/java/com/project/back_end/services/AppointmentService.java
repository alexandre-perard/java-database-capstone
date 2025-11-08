package com.project.back_end.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.models.Appointment;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

@Service
public class AppointmentService {

	private final AppointmentRepository appointmentRepository;
	private final PatientRepository patientRepository;
	private final DoctorRepository doctorRepository;

	@Autowired
	public AppointmentService(AppointmentRepository appointmentRepository, PatientRepository patientRepository,
			DoctorRepository doctorRepository) {
		this.appointmentRepository = appointmentRepository;
		this.patientRepository = patientRepository;
		this.doctorRepository = doctorRepository;
	}

	@Transactional
	public int bookAppointment(Appointment appointment) {
		try {
			appointmentRepository.save(appointment);
			return 1;
		} catch (Exception e) {
			return 0;
		}
	}

	@Transactional
	public boolean updateAppointment(Appointment appointment) {
		try {
			if (appointment.getId() == null) return false;
			if (!appointmentRepository.existsById(appointment.getId())) return false;
			appointmentRepository.save(appointment);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Transactional
	public boolean cancelAppointment(Long appointmentId, Long patientId) {
		try {
			Appointment a = appointmentRepository.findById(appointmentId).orElse(null);
			if (a == null) return false;
			if (a.getPatient() == null || !a.getPatient().getId().equals(patientId)) return false;
			appointmentRepository.deleteById(appointmentId);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public List<Appointment> getAllAppointments(LocalDateTime date, String patientName, String token) {
		// For simplicity return all appointments that match the date (day)
		// Caller can filter further. Here we just return appointments for the day.
		LocalDateTime start = date.toLocalDate().atStartOfDay();
		LocalDateTime end = start.plusDays(1);
		// If patientName is 'null' or null, return all for the day
		if (patientName == null || patientName.equals("null")) {
			return appointmentRepository.findByAppointmentTimeBetween(start, end);
		}
		// otherwise filter by patient name
		return appointmentRepository.findByPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
				patientName, start, end);
	}

	@Transactional(readOnly = true)
	public int validateAppointment(Long doctorId, LocalDateTime appointmentTime) {
		if (doctorId == null || appointmentTime == null) return 0;
		// check doctor exists
		if (!doctorRepository.existsById(doctorId)) return -1;
		LocalDateTime start = appointmentTime.toLocalDate().atStartOfDay();
		LocalDateTime end = start.plusDays(1);
		List<Appointment> apps = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);
		for (Appointment a : apps) {
			if (a.getAppointmentTime() != null && a.getAppointmentTime().isEqual(appointmentTime)) {
				return 0; // slot already taken
			}
		}
		return 1; // valid
	}

	@Transactional
	public boolean changeStatus(Long id, Integer status) {
		try {
			appointmentRepository.updateStatus(status, id);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
