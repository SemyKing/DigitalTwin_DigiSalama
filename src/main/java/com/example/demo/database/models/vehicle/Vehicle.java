package com.example.demo.database.models.vehicle;

import com.example.demo.database.models.Organisation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String name;

	@Column
	private String vin;

	@Column
	private String registrationNumber;

	@ManyToOne
	@JoinColumn(name="fleet_id", referencedColumnName = "id")
	private Fleet fleet;

	@ManyToOne
	@JoinColumn(name="organisation_id", referencedColumnName = "id")
	private Organisation organisation;

	@Transient
	@Column
	private Boolean isSelected = false;

	@Column
	private Boolean isDeleted = false;
}
