package com.project.back_end.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.PatientService;

@org.springframework.stereotype.Service
public class Service {
	private final TokenService tokenService;
	private final AdminRepository adminRepository;
	private final DoctorRepository doctorRepository;
	private final PatientRepository patientRepository;
	private final AppointmentService appointmentService;
	private final PatientService patientService;

	@Autowired
	public Service(TokenService tokenService, AdminRepository adminRepository, DoctorRepository doctorRepository,
			PatientRepository patientRepository, AppointmentService appointmentService, PatientService patientService) {
		this.tokenService = tokenService;
		this.adminRepository = adminRepository;
		this.doctorRepository = doctorRepository;
		this.patientRepository = patientRepository;
		this.appointmentService = appointmentService;
		this.patientService = patientService;
	}

	public boolean validateToken(String token, String role) {
		return tokenService.validateToken(token, role);
	}

	public Admin validateAdminLogin(String username, String password) {
		Admin admin = adminRepository.findByUsername(username);
		if (admin == null) return null;
		if (admin.getPassword() != null && admin.getPassword().equals(password)) return admin;
		return null;
	}

	public boolean validatePatient(String email, String phone) {
		Patient byEmail = patientRepository.findByEmail(email);
		if (byEmail != null) return false;
		Patient byPhone = null;
		try {
			byPhone = patientRepository.findByEmailOrPhone(email, phone);
		} catch (Exception e) {
			// ignore
		}
		return byPhone == null;
	}

	public Patient validatePatientLogin(String email, String password) {
		Patient p = patientRepository.findByEmail(email);
		if (p == null) return null;
		if (p.getPassword() != null && p.getPassword().equals(password)) return p;
		return null;
	}

	public List<Doctor> filterDoctor(String name, String time, String specialty) {
		// Very simple filter: delegate to repository methods where possible
		if ((name == null || name.equals("null")) && (specialty == null || specialty.equals("null"))) {
			return doctorRepository.findAll();
		}
		if (name != null && specialty != null) {
			return doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
		}
		if (name != null) {
			return doctorRepository.findByNameLike("%" + name + "%");
		}
		if (specialty != null) {
			return doctorRepository.findBySpecialtyIgnoreCase(specialty);
		}
		return Collections.emptyList();
	}
    
	/**
	 * Validate an appointment slot for a doctor at a given time.
	 * Delegates to AppointmentService.validateAppointment which returns:
	 *  -1 if doctor not found, 0 if slot taken, 1 if valid
	 */
	public int validateAppointment(Long doctorId, LocalDateTime appointmentTime) {
		return appointmentService.validateAppointment(doctorId, appointmentTime);
	}

	/**
	 * Filter patient appointments using the token to identify the patient.
	 * Delegates to PatientService for actual filtering logic.
	 */
	public List<AppointmentDTO> filterPatient(String token, String condition, String doctorName) {
		if (token == null) return new ArrayList<>();
		String email = tokenService.extractEmail(token);
		if (email == null) return new ArrayList<>();
		Patient p = patientRepository.findByEmail(email);
		if (p == null) return new ArrayList<>();
		Long patientId = p.getId();
		boolean hasCondition = condition != null && !condition.equals("null");
		boolean hasDoctor = doctorName != null && !doctorName.equals("null");
		if (hasCondition && hasDoctor) {
			return patientService.filterByDoctorAndCondition(patientId, doctorName, condition);
		}
		if (hasCondition) {
			return patientService.filterByCondition(patientId, condition);
		}
		if (hasDoctor) {
			return patientService.filterByDoctor(patientId, doctorName);
		}
		return patientService.getPatientAppointments(patientId);
	}
// The @Service annotation marks this class as a service component in Spring. This allows Spring to automatically detect it through component scanning
// and manage its lifecycle, enabling it to be injected into controllers or other services using @Autowired or constructor injection.

// 2. **Constructor Injection for Dependencies**
// The constructor injects all required dependencies (TokenService, Repositories, and other Services). This approach promotes loose coupling, improves testability,
// and ensures that all required dependencies are provided at object creation time.

// 3. **validateToken Method**
// This method checks if the provided JWT token is valid for a specific user. It uses the TokenService to perform the validation.
// If the token is invalid or expired, it returns a 401 Unauthorized response with an appropriate error message. This ensures security by preventing
// unauthorized access to protected resources.

// 4. **validateAdmin Method**
// This method validates the login credentials for an admin user.
// - It first searches the admin repository using the provided username.
// - If an admin is found, it checks if the password matches.
// - If the password is correct, it generates and returns a JWT token (using the admin’s username) with a 200 OK status.
// - If the password is incorrect, it returns a 401 Unauthorized status with an error message.
// - If no admin is found, it also returns a 401 Unauthorized.
// - If any unexpected error occurs during the process, a 500 Internal Server Error response is returned.
// This method ensures that only valid admin users can access secured parts of the system.

// 5. **filterDoctor Method**
// This method provides filtering functionality for doctors based on name, specialty, and available time slots.
// - It supports various combinations of the three filters.
// - If none of the filters are provided, it returns all available doctors.
// This flexible filtering mechanism allows the frontend or consumers of the API to search and narrow down doctors based on user criteria.

// 6. **validateAppointment Method**
// This method validates if the requested appointment time for a doctor is available.
// - It first checks if the doctor exists in the repository.
// - Then, it retrieves the list of available time slots for the doctor on the specified date.
// - It compares the requested appointment time with the start times of these slots.
// - If a match is found, it returns 1 (valid appointment time).
// - If no matching time slot is found, it returns 0 (invalid).
// - If the doctor doesn’t exist, it returns -1.
// This logic prevents overlapping or invalid appointment bookings.

// 7. **validatePatient Method**
// This method checks whether a patient with the same email or phone number already exists in the system.
// - If a match is found, it returns false (indicating the patient is not valid for new registration).
// - If no match is found, it returns true.
// This helps enforce uniqueness constraints on patient records and prevent duplicate entries.

// 8. **validatePatientLogin Method**
// This method handles login validation for patient users.
// - It looks up the patient by email.
// - If found, it checks whether the provided password matches the stored one.
// - On successful validation, it generates a JWT token and returns it with a 200 OK status.
// - If the password is incorrect or the patient doesn't exist, it returns a 401 Unauthorized with a relevant error.
// - If an exception occurs, it returns a 500 Internal Server Error.
// This method ensures only legitimate patients can log in and access their data securely.

// 9. **filterPatient Method**
// This method filters a patient's appointment history based on condition and doctor name.
// - It extracts the email from the JWT token to identify the patient.
// - Depending on which filters (condition, doctor name) are provided, it delegates the filtering logic to PatientService.
// - If no filters are provided, it retrieves all appointments for the patient.
// This flexible method supports patient-specific querying and enhances user experience on the client side.


}
