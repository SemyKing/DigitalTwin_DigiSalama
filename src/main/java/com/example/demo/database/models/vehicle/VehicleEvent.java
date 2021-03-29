package com.example.demo.database.models.vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String name;

	@ManyToOne
	@JoinColumn(name="vehicle_id", referencedColumnName = "id")
	private Vehicle vehicle;

	@Column(columnDefinition="TEXT")
	private String description;

	@Column(columnDefinition = "TIMESTAMP")
	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp = new Date();
}
