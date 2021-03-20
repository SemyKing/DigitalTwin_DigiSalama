package com.example.demo.database.models.vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
	private String name;

	@Column
	private String type;

	@Column(columnDefinition="TEXT")
	private String description;

	@Lob
	@Column
	private byte[] data;

	@ManyToOne
	@JoinColumn(name="vehicle_id", referencedColumnName = "id")
	private Vehicle vehicle;

	@Column
	private Boolean isDeleted = false;

	public FileDB(String name, String type, byte[] data) {
		this.name = name;
		this.type = type;
		this.data = data;
	}
}
