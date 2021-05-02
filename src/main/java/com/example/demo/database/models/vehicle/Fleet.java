package com.example.demo.database.models.vehicle;

import com.example.demo.database.models.Organisation;
import com.fasterxml.jackson.annotation.*;
import lombok.*;
import org.apache.commons.lang3.builder.HashCodeExclude;

import javax.persistence.*;
import java.util.*;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fleet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;


	@Column
	private String name;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@JsonIgnoreProperties("fleets")
	@ManyToMany(mappedBy = "fleets", fetch = FetchType.EAGER)
	private Set<Vehicle> vehicles = new HashSet<>();


	@Transient
	@JsonIgnore
	@ToString.Include
	@EqualsAndHashCode.Exclude
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


//	@Override
//	public String toString() {
//		return "Fleet(id=" + id + ", name=" + name + ", vehicle_ids=" + getVehicle_ids() + ", organisation=" + organisation;
//	}
}
