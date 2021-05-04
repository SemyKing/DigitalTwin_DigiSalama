package com.example.demo.database.models.vehicle;

import com.example.demo.utils.LocalDateTimeConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

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

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@JsonIgnoreProperties({"vehicle_event"})
	@OneToMany(mappedBy = "vehicle_event")
	private Set<FileMetaData> files = new HashSet<>();


	@Transient
	@JsonIgnore
	@ToString.Include
	@EqualsAndHashCode.Exclude
	@Getter(AccessLevel.NONE)
	private List<Long> files_ids = new ArrayList<>();

	// CUSTOM GETTER
	public List<Long> getFiles_ids() {
		files_ids.clear();

		for (FileMetaData file : files) {
			files_ids.add(file.getId());
		}

		Collections.sort(files_ids);

		return files_ids;
	}


	@Column
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@Convert(converter = LocalDateTimeConverter.class)
	private LocalDateTime timestamp = LocalDateTime.now();
}
