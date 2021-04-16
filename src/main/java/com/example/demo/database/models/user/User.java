package com.example.demo.database.models.user;

import com.example.demo.database.models.Organisation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"password_update_token", "password"})
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String username;

	@Column
	private String password;

	@Column
	private String first_name;

	@Column
	private String last_name;

	@Column
	private String email;

	@OneToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "organisation_id", referencedColumnName = "id")
	private Organisation organisation;

	@OneToOne
	@JoinColumn(name = "role_id", referencedColumnName = "id")
	private Role role;

	@Column
	private String password_update_token = null;
}
