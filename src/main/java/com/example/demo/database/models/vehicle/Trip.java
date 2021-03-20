package com.example.demo.database.models.vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trip {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String name;

	@Column
	private String origin;

	@Column
	private String destination;

	@Column
	private Integer kiloMetresDriven;

	@ManyToOne
	@JoinColumn(name="vehicle_id", referencedColumnName = "id")
	private Vehicle vehicle;

	@Column
	private Boolean isDeleted = false;
}
