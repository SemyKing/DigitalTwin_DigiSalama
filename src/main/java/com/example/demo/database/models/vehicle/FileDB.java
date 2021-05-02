package com.example.demo.database.models.vehicle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String file_name;

	@Column
	private String file_type;

	@Lob
	@Column
	@ToString.Exclude
	private byte[] data;

	@Column(columnDefinition="TEXT")
	private String description;

	@ManyToOne
	@JoinColumn(name="vehicle_id", referencedColumnName = "id")
	@ToString.Exclude
	private Vehicle vehicle;

	@Transient
	@JsonIgnore
	@ToString.Include
	private Long vehicle_id() {
		return this.vehicle == null ? null : this.vehicle.getId();
	}

	@ManyToOne
	@JoinColumn(name="refuel_id", referencedColumnName = "id")
	private Refuel refuel;

	@ManyToOne
	@JoinColumn(name="event_id", referencedColumnName = "id")
	private VehicleEvent vehicle_event;


	public FileDB(String file_name, String file_type, byte[] data) {
		this.file_name = file_name;
		this.file_type = file_type;
		this.data = data;
	}
}
