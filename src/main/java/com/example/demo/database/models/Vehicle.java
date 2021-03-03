package com.example.demo.database.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;
import java.util.Set;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column
	private String name;

	@Column
	private String vin;

	@Column
	private String registrationPlate;

	@ManyToMany
	@JoinTable(
			name = "vehicle_fleets",
			joinColumns = @JoinColumn(name = "vehicle_id", referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "vehicle_fleet_id", referencedColumnName = "id"))
	private Collection<VehicleFleet> vehicleFleets;

	@OneToMany(mappedBy="vehicle")
	private Set<Image> images;


	@Column
	private Boolean isDeleted = false;
}
