package com.project.back_end.DTO;

public class Login {
	// 1. 'email' field:
//    - Type: private String
//    - Description:
//      - Represents the email address used for logging into the system.
//      - The email field is expected to contain a valid email address for user authentication purposes.

	// 2. 'password' field:
//    - Type: private String
//    - Description:
//      - Represents the password associated with the email address.
//      - The password field is used for verifying the user's identity during login.
//      - It is generally hashed before being stored and compared during authentication.

	private String email;
	private String password;

	public Login() {
		// default constructor
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "Login{" +
				"email='" + email + '\'' +
				", password='[PROTECTED]'" +
				'}';
	}

	// 3. Constructor: default provided above
	// 4. Getters and Setters implemented

}
