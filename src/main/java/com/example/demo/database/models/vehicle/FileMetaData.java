package com.example.demo.database.models.vehicle;

import com.example.demo.utils.LocalDateTimeConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMetaData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(cascade = CascadeType.REMOVE)
	private FileByteData file_byte_data;

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

	@Column
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@Convert(converter = LocalDateTimeConverter.class)
	private LocalDateTime timestamp = LocalDateTime.now();


	@Transient
	@JsonIgnore
	@ToString.Exclude
	private Boolean isSelected = false;
}
