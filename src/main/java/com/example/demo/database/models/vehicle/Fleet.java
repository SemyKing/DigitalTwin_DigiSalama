package com.example.demo.database.models.vehicle;

import com.example.demo.database.models.Organisation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.*;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Fleet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;


	@Column
	private String name;

	/**
	 * This Set is not visible in toString() nor in JSON GET requests
	 * {@link #vehicle_ids} is displayed instead
	 * because {@link #vehicles} and {@link Vehicle#fleets} are in ManyToMany relationship and recursively call each other in toString() and JSON GET
	 *
	 * however this Set must be used in POST, PATCH and PUT requests containing Vehicle objects with IDs (other parameters are not necessary)
	 */
	@JsonIgnore
	@EqualsAndHashCode.Exclude
	@ManyToMany(mappedBy = "fleets", fetch = FetchType.EAGER)
	private Set<Vehicle> vehicles = new HashSet<>();


	@Transient
	@Getter(AccessLevel.NONE)
	private List<Long> vehicle_ids = new ArrayList<>();

	// CUSTOM GETTER
	public List<Long> getVehicle_ids() {
		vehicle_ids.clear();

		for (Vehicle vehicle : vehicles) {
			vehicle_ids.add(vehicle.getId());
		}

		Collections.sort(vehicle_ids);

		return vehicle_ids;
	}


	@OneToOne
	@JoinColumn(name="organisation_id", referencedColumnName = "id")
	private Organisation organisation;


	@Transient
	@JsonIgnore
	private Boolean isSelected = false;


	@Override
	public String toString() {
		return "Fleet(id=" + id + ", name=" + name + ", vehicle_ids=" + getVehicle_ids() + ", organisation=" + organisation;
	}
}
