package com.example.demo.database.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSettings {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private boolean user_event_logging = false;

	@Column
	private boolean organisation_event_logging = false;

	@Column
	private boolean fleet_event_logging = false;

	@Column
	private boolean vehicle_event_logging = false;

	@Column
	private boolean vehicle_event_event_logging = false;

	@Column
	private boolean distance_event_logging = false;

	@Column
	private boolean refuel_event_logging = false;

	@Column
	private boolean trip_event_logging = false;

	@Column
	private boolean equipment_event_logging = false;

	@Column
	private boolean equipment_type_event_logging = false;

	@Column
	private boolean file_event_logging = false;
}
