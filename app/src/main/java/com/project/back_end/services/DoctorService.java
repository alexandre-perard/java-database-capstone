package com.project.back_end.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;

@Service
public class DoctorService {

	private final DoctorRepository doctorRepository;
	private final AppointmentRepository appointmentRepository;
	private final TokenService tokenService;

	@Autowired
	public DoctorService(DoctorRepository doctorRepository, AppointmentRepository appointmentRepository,
			TokenService tokenService) {
		this.doctorRepository = doctorRepository;
		this.appointmentRepository = appointmentRepository;
		this.tokenService = tokenService;
	}

	public List<Doctor> getDoctors() {
		return doctorRepository.findAll();
	}

	@Transactional
	public int saveDoctor(Doctor doctor, String token) {
		try {
			Doctor existing = doctorRepository.findByEmail(doctor.getEmail());
			if (existing != null) return -1; // conflict
			doctorRepository.save(doctor);
			return 1;
		} catch (Exception e) {
			return 0;
		}
	}

	@Transactional
	public int deleteDoctor(Long id) {
		try {
			if (!doctorRepository.existsById(id)) return -1;
			appointmentRepository.deleteAllByDoctorId(id);
			doctorRepository.deleteById(id);
			return 1;
		} catch (Exception e) {
			return 0;
		}
	}

	public String validateDoctor(String email, String password) {
		Doctor d = doctorRepository.findByEmail(email);
		if (d == null) return null;
		if (d.getPassword() != null && d.getPassword().equals(password)) {
			return tokenService.generateToken(email);
		}
		return null;
	}

	public List<Doctor> findDoctorByName(String name) {
		return doctorRepository.findByNameLike("%" + name + "%");
	}

	// Additional helper filters can be implemented as needed

    public List<String> getDoctorAvailability(Long doctorId, java.time.LocalDateTime date) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null || doctor.getAvailableTimes() == null) return java.util.Collections.emptyList();
        // Get all appointments for the doctor on the given date
        java.time.LocalDateTime start = date.toLocalDate().atStartOfDay();
        java.time.LocalDateTime end = start.plusDays(1);
        List<com.project.back_end.models.Appointment> apps = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);
        // Collect booked times
        java.util.Set<String> bookedTimes = new java.util.HashSet<>();
        for (com.project.back_end.models.Appointment a : apps) {
            if (a.getAppointmentTime() != null) {
                bookedTimes.add(a.getAppointmentTime().toLocalTime().toString());
            }
        }
        // Calculate available times
        List<String> availability = new java.util.ArrayList<>();
        for (String time : doctor.getAvailableTimes()) {
            if (!bookedTimes.contains(time)) {
                availability.add(time);
            }
        }
        return availability;
    }
	@Transactional
	public int updateDoctor(Doctor doctor) {
		try {
			if (doctor == null || doctor.getId() == null) return -1;
			if (!doctorRepository.existsById(doctor.getId())) return -1;
			doctorRepository.save(doctor);
			return 1;
		} catch (Exception e) {
			return 0;
		}
	}

	@Transactional(readOnly = true)
	public List<Doctor> filterDoctorsByNameSpecilityandTime(String name, String specialty, String time) {
		// reuse repository filters and then apply time filtering if requested
		List<Doctor> base;
		if (name != null && specialty != null) {
			base = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
		} else if (name != null) {
			base = doctorRepository.findByNameLike("%" + name + "%");
		} else if (specialty != null) {
			base = doctorRepository.findBySpecialtyIgnoreCase(specialty);
		} else {
			base = doctorRepository.findAll();
		}
		if (time == null || time.equals("null")) return base;
		return filterDoctorByTime(base, time);
	}

	@Transactional(readOnly = true)
	public List<Doctor> filterDoctorByTime(List<Doctor> doctors, String period) {
		if (doctors == null) return java.util.Collections.emptyList();
		List<Doctor> out = new java.util.ArrayList<>();
		for (Doctor d : doctors) {
			List<String> times = d.getAvailableTimes();
			if (times == null) continue;
			for (String t : times) {
				if (matchesPeriod(t, period)) {
					out.add(d);
					break;
				}
			}
		}
		return out;
	}

	@Transactional(readOnly = true)
	public List<Doctor> filterDoctorByNameAndTime(String name, String period) {
		List<Doctor> base = doctorRepository.findByNameLike("%" + name + "%");
		return filterDoctorByTime(base, period);
	}

	@Transactional(readOnly = true)
	public List<Doctor> filterDoctorByNameAndSpecility(String name, String specialty) {
		return doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
	}

	@Transactional(readOnly = true)
	public List<Doctor> filterDoctorByTimeAndSpecility(String specialty, String period) {
		List<Doctor> base = doctorRepository.findBySpecialtyIgnoreCase(specialty);
		return filterDoctorByTime(base, period);
	}

	@Transactional(readOnly = true)
	public List<Doctor> filterDoctorBySpecility(String specialty) {
		return doctorRepository.findBySpecialtyIgnoreCase(specialty);
	}

	@Transactional(readOnly = true)
	public List<Doctor> filterDoctorsByTime(String period) {
		List<Doctor> all = doctorRepository.findAll();
		return filterDoctorByTime(all, period);
	}

	private boolean matchesPeriod(String timeStr, String period) {
		if (timeStr == null || period == null) return false;
		try {
			String[] parts = timeStr.split(":");
			int hour = Integer.parseInt(parts[0]);
			if (period.equalsIgnoreCase("am")) return hour < 12;
			if (period.equalsIgnoreCase("pm")) return hour >= 12;
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
