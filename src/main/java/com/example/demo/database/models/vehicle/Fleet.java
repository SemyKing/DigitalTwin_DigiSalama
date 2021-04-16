package com.example.demo.database.models.vehicle;

import com.example.demo.database.models.Organisation;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude="vehicles")
@ToString(exclude = "vehicles")
public class Fleet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String name;

	@ManyToMany(mappedBy = "fleets")
	private Set<Vehicle> vehicles;

	@OneToOne
	@JoinColumn(name="organisation_id", referencedColumnName = "id")
	private Organisation organisation;


	@Transient
	private Boolean isSelected = false;
}
