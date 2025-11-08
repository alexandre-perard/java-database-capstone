package com.project.back_end.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.models.Admin;
import com.project.back_end.services.Service;
import com.project.back_end.services.TokenService;

@RestController
@RequestMapping("${api.path}admin")
public class AdminController {

	private final Service service;
	private final TokenService tokenService;

	@Autowired
	public AdminController(Service service, TokenService tokenService) {
		this.service = service;
		this.tokenService = tokenService;
	}

	@PostMapping("/login")
	public ResponseEntity<?> adminLogin(@RequestBody Admin admin) {
		if (admin == null || admin.getUsername() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Missing credentials"));
		}
		Admin valid = service.validateAdminLogin(admin.getUsername(), admin.getPassword());
		if (valid == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
		}
		String token = tokenService.generateToken(valid.getUsername());
		Map<String, Object> resp = new HashMap<>();
		resp.put("status", "ok");
		resp.put("token", token);
		return ResponseEntity.ok(resp);
	}

}

