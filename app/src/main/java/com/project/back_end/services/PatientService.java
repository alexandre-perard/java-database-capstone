package com.project.back_end.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.services.TokenService;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    @Autowired
    public PatientService(PatientRepository patientRepository, AppointmentRepository appointmentRepository,
            TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    @Transactional
    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public List<AppointmentDTO> getPatientAppointments(Long patientId) {
        List<Appointment> apps = appointmentRepository.findByPatientId(patientId);
        return toDtos(apps);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> filterByCondition(Long patientId, String condition) {
        if (condition == null) return java.util.Collections.emptyList();
        int status;
        if (condition.equalsIgnoreCase("past")) status = 1;
        else if (condition.equalsIgnoreCase("future")) status = 0;
        else return java.util.Collections.emptyList();

        List<Appointment> apps = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patientId, status);
        return toDtos(apps);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> filterByDoctor(Long patientId, String doctorName) {
        if (doctorName == null) return java.util.Collections.emptyList();
        List<Appointment> apps = appointmentRepository.filterByDoctorNameAndPatientId(doctorName, patientId);
        return toDtos(apps);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> filterByDoctorAndCondition(Long patientId, String doctorName, String condition) {
        if (doctorName == null || condition == null) return java.util.Collections.emptyList();
        int status = condition.equalsIgnoreCase("past") ? 1 : 0;
        List<Appointment> apps = appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(doctorName, patientId, status);
        return toDtos(apps);
    }

    @Transactional(readOnly = true)
    public Patient getPatientDetails(String token) {
        if (token == null) return null;
        String email = tokenService.extractEmail(token);
        if (email == null) return null;
        return patientRepository.findByEmail(email);
    }

    private List<AppointmentDTO> toDtos(List<Appointment> apps) {
        List<AppointmentDTO> dtos = new ArrayList<>();
        if (apps == null) return dtos;
        for (Appointment a : apps) {
            AppointmentDTO dto = new AppointmentDTO(
                    a.getId(),
                    a.getDoctor() != null ? a.getDoctor().getId() : null,
                    a.getDoctor() != null ? a.getDoctor().getName() : null,
                    a.getPatient() != null ? a.getPatient().getId() : null,
                    a.getPatient() != null ? a.getPatient().getName() : null,
                    a.getPatient() != null ? a.getPatient().getEmail() : null,
                    a.getPatient() != null ? a.getPatient().getPhone() : null,
                    a.getPatient() != null ? a.getPatient().getAddress() : null,
                    a.getAppointmentTime(),
                    a.getStatus() != null ? a.getStatus() : 0);
            dtos.add(dto);
        }
        return dtos;
    }

    // Additional filter helpers can be added here as needed

}

// 5. **filterByCondition Method**:
//    - Filters appointments for a patient based on the condition (e.g., "past" or "future").
//    - Retrieves appointments with a specific status (0 for future, 1 for past) for the patient.
//    - Converts the appointments into `AppointmentDTO` and returns them in the response.
//    - Instruction: Ensure the method correctly handles "past" and "future" conditions, and that invalid conditions are caught and returned as errors.

// 6. **filterByDoctor Method**:
//    - Filters appointments for a patient based on the doctor's name.
//    - It retrieves appointments where the doctor’s name matches the given value, and the patient ID matches the provided ID.
//    - Instruction: Ensure that the method correctly filters by doctor's name and patient ID and handles any errors or invalid cases.

// 7. **filterByDoctorAndCondition Method**:
//    - Filters appointments based on both the doctor's name and the condition (past or future) for a specific patient.
//    - This method combines filtering by doctor name and appointment status (past or future).
//    - Converts the appointments into `AppointmentDTO` objects and returns them in the response.
//    - Instruction: Ensure that the filter handles both doctor name and condition properly, and catches errors for invalid input.

// 8. **getPatientDetails Method**:
//    - Retrieves patient details using the `tokenService` to extract the patient's email from the provided token.
//    - Once the email is extracted, it fetches the corresponding patient from the `patientRepository`.
//    - It returns the patient's information in the response body.
    //    - Instruction: Make sure that the token extraction process works correctly and patient details are fetched properly based on the extracted email.

// 9. **Handling Exceptions and Errors**:
//    - The service methods handle exceptions using try-catch blocks and log any issues that occur. If an error occurs during database operations, the service responds with appropriate HTTP status codes (e.g., `500 Internal Server Error`).
//    - Instruction: Ensure that error handling is consistent across the service, with proper logging and meaningful error messages returned to the client.

// 10. **Use of DTOs (Data Transfer Objects)**:
//    - The service uses `AppointmentDTO` to transfer appointment-related data between layers. This ensures that sensitive or unnecessary data (e.g., password or private patient information) is not exposed in the response.
//    - Instruction: Ensure that DTOs are used appropriately to limit the exposure of internal data and only send the relevant fields to the client.


