package com.project.back_end.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.project.back_end.services.Service;

@Controller
public class DashboardController {

	private final Service service;

	@Autowired
	public DashboardController(Service service) {
		this.service = service;
	}

	@GetMapping("/adminDashboard/{token}")
	public String adminDashboard(@PathVariable("token") String token) {
		try {
			boolean valid = service != null && service.validateToken(token, "admin");
			if (valid) {
				return "admin/adminDashboard";
			}
		} catch (Exception e) {
			// log if a logging framework is available; keep simple here
			System.err.println("Token validation error for admin: " + e.getMessage());
		}
		return "redirect:/";
	}

	@GetMapping("/doctorDashboard/{token}")
	public String doctorDashboard(@PathVariable("token") String token) {
		try {
			boolean valid = service != null && service.validateToken(token, "doctor");
			if (valid) {
				return "doctor/doctorDashboard";
			}
		} catch (Exception e) {
			System.err.println("Token validation error for doctor: " + e.getMessage());
		}
		return "redirect:/";
	}

}
