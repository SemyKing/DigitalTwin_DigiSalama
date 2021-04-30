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
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String username;

	@Column
	@ToString.Exclude
	private String password;

	@Column
	@ToString.Exclude
	private String first_name;

	@Column
	@ToString.Exclude
	private String last_name;

	@Column
	@ToString.Exclude
	private String email;

	@OneToOne
	@JoinColumn(name = "organisation_id", referencedColumnName = "id")
	private Organisation organisation;

	@OneToOne
	@ToString.Exclude
	@JoinColumn(name = "role_id", referencedColumnName = "id")
	private Role role;

	@Column
	@ToString.Exclude
	private String password_update_token = null;
}
