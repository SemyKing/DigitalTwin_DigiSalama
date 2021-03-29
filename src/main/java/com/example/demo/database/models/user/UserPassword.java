package com.example.demo.database.models.user;

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
 * Class is used as a wrapping object for changing password
 */
public class UserPassword {

	private String current_password;
	private String new_password_1;
	private String new_password_2;
}
