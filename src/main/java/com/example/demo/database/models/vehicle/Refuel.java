package com.example.demo.database.models.vehicle;

import com.example.demo.utils.LocalDateTimeConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Refuel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String location;

	@Column
	private String fuel_name;

	@Column
	private Float refuel_amount;

	@Column
	private Float price;

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


	// USED IN UI (THYMELEAF HTML)
	@Transient
	@JsonIgnore
	@ToString.Exclude
	@Getter(AccessLevel.NONE)
	private String short_description;

	public String getShort_description() {
		return "id:" + id + ", location: " + location + ", fuel name: " + fuel_name + ", refuel amount: " + refuel_amount;
	}
}
