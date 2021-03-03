package com.example.demo.database.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column
	private String username;

	@Column
	private String passwordHash;

	@Column
	private String firstName;

	@Column
	private String lastName;

	@Column
	private String email;

	private String apiToken;

	@ManyToOne
	@JoinColumn(name="organisation_id", referencedColumnName = "id")
	private Organisation organisation;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "role_id", referencedColumnName = "id")
	private Role role;

	@Column
	private Boolean isDeleted = false;

}
