package com.example.demo.database.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Class is used as a wrapping object for login and changing password
 */
public class UserPassword {

	private String username;

	private String email;

	private String currentPassword;

	private String newPassword1;

	private String newPassword2;
}
