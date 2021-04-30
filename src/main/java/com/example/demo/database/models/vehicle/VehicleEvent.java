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
	@ToString.Exclude
	private Vehicle vehicle;

	@Transient
	@JsonIgnore
	@ToString.Include
	private Long vehicle_id() {
		return this.vehicle == null ? null : this.vehicle.getId();
	}

	@Column(columnDefinition="TEXT")
	private String description;

	@Column
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@Convert(converter = LocalDateTimeConverter.class)
	private LocalDateTime timestamp = LocalDateTime.now();
}
