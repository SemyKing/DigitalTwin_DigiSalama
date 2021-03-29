package com.example.demo.database.models.vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

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

	@Column(columnDefinition="TEXT")
	private String description;

	@Lob
	@Column
	private byte[] data;

	@ManyToOne
	@JoinColumn(name="vehicle_id", referencedColumnName = "id")
	private Vehicle vehicle;

	@ManyToOne
	@JoinColumn(name="refuel_id", referencedColumnName = "id")
	private Refuel refuel;

	@ManyToOne
	@JoinColumn(name="event_id", referencedColumnName = "id")
	private VehicleEvent event;


	@Transient
	private MultipartFile multipart_file;


	public FileDB(String file_name, String file_type, byte[] data) {
		this.file_name = file_name;
		this.file_type = file_type;
		this.data = data;
	}
}
