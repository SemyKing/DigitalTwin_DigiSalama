package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.*;
import com.example.demo.database.repositories.vehicle.*;
import com.example.demo.utils.DateUtils;
import com.example.demo.utils.FieldReflectionUtils;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileService {

	private final FileMetaDataRepository fileMetaDataRepository;
	private final FileByteDataRepository fileByteDataRepository;

	private final VehicleRepository vehicleRepository;
	private final RefuelRepository refuelRepository;
	private final VehicleEventRepository vehicleEventRepository;


	@Transactional
	public List<FileMetaData> getAll() {
		return fileMetaDataRepository.findAll();
	}

	@Transactional
	public FileMetaData getFileMetaDataById(Long id) {
		if (id == null) {
			return null;
		}

		Optional<FileMetaData> fileMetaData = fileMetaDataRepository.findById(id);
		if (fileMetaData.isEmpty()) {
			return null;
		}
		return fileMetaData.get();
	}

	@Transactional
	public FileByteData getFileByteDataById(Long id) {
		if (id == null) {
			return null;
		}

		Optional<FileByteData> fileByteData = fileByteDataRepository.findById(id);
		if (fileByteData.isEmpty()) {
			return null;
		}
		return fileByteData.get();
	}

	@Transactional
	public List<FileMetaData> getAllByVehicleId(Long id) {
		if (id == null) {
			return null;
		}

		return fileMetaDataRepository.findAllByVehicleId(id);
	}

	@Transactional
	public List<FileMetaData> getAllByRefuelId(Long id) {
		if (id == null) {
			return null;
		}

		return fileMetaDataRepository.findAllByRefuelId(id);
	}

	@Transactional
	public List<FileMetaData> getAllByVehicleEventId(Long id) {
		if (id == null) {
			return null;
		}

		return fileMetaDataRepository.findAllByVehicleEventId(id);
	}

	@Transactional
	public FileMetaData save(FileMetaData file) {
		return fileMetaDataRepository.save(file);
	}

	@Transactional
	public FileByteData save(FileByteData file) {
		return fileByteDataRepository.save(file);
	}

	@Transactional
	public FileByteData save(MultipartFile file) throws IOException {
		String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

		if (fileName.length() <= 0) {
			fileName = "temp_file" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
		}

		if (file.getContentType() == null) {
			throw new FileUploadException("File content type cannot be NULL");
		}

		FileByteData fileByteData = new FileByteData();
		fileByteData.setData(file.getBytes());
		fileByteData.setFile_name(fileName);
		fileByteData.setFile_content_type(file.getContentType());

		return fileByteDataRepository.save(fileByteData);
	}

	@Transactional
	public void delete(FileMetaData fileMetaData) {
		if (fileMetaData == null) {
			return;
		}

		if (fileMetaData.getId() == null) {
			return;
		}

		fileMetaDataRepository.delete(fileMetaData);
	}

	@Transactional
	public void delete(FileByteData fileByteData) {
		if (fileByteData == null) {
			return;
		}

		if (fileByteData.getId() == null) {
			return;
		}

		fileByteDataRepository.delete(fileByteData);
	}

	@Transactional
	public void deleteAll() {
		fileMetaDataRepository.deleteAll();
	}


	public String getImageData(byte[] byteData) {
		return Base64.getMimeEncoder().encodeToString(byteData);
	}

	public ValidationResponse validate(FileMetaData file, Mapping mapping) {

		if (file == null) {
			return new ValidationResponse(false, "provided NULL entity");
		}

		if (mapping.equals(Mapping.POST)) {
			file.setId(null);
		}

		if (mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {
			if (file.getId() == null) {
				return new ValidationResponse(false, "entity ID parameter is required");
			}

			FileMetaData fileFromDatabase = getFileMetaDataById(file.getId());

			if (fileFromDatabase == null) {
				return new ValidationResponse(false, "entity ID parameter is invalid");
			}
		}

		if (mapping.equals(Mapping.POST) || mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {

			if (file.getFile_byte_data() != null) {
				if (file.getFile_byte_data().getId() == null) {
					return new ValidationResponse(false, "file_byte_data ID is required");
				}

				Optional<FileByteData> fileByteData = fileByteDataRepository.findById(file.getFile_byte_data().getId());

				if (fileByteData.isEmpty()) {
					return new ValidationResponse(false, "file_byte_data with ID: " + file.getFile_byte_data().getId() + " not found");
				}

				file.setFile_byte_data(fileByteData.get());
			}

			if (file.getVehicle() != null) {
				if (file.getVehicle().getId() == null) {
					return new ValidationResponse(false, "vehicle ID is required");
				}

				Optional<Vehicle> vehicle = vehicleRepository.findById(file.getVehicle().getId());

				if (vehicle.isEmpty()) {
					return new ValidationResponse(false, "vehicle with ID: " + file.getVehicle().getId() + " not found");
				}

				file.setVehicle(vehicle.get());
			}

			if (file.getRefuel() != null) {
				if (file.getRefuel().getId() == null) {
					return new ValidationResponse(false, "refuel ID is required");
				}

				Optional<Refuel> refuel = refuelRepository.findById(file.getRefuel().getId());

				if (refuel.isEmpty()) {
					return new ValidationResponse(false, "refuel with ID: " + file.getRefuel().getId() + " not found");
				}

				file.setRefuel(refuel.get());
			}

			if (file.getVehicle_event() != null) {
				if (file.getVehicle_event().getId() == null) {
					return new ValidationResponse(false, "vehicle_event ID is required");
				}

				Optional<VehicleEvent> vehicleEvent = vehicleEventRepository.findById(file.getVehicle_event().getId());

				if (vehicleEvent.isEmpty()) {
					return new ValidationResponse(false, "vehicle_event with ID: " + file.getVehicle_event().getId() + " not found");
				}

				file.setVehicle_event(vehicleEvent.get());
			}

			ValidationResponse stringFieldsValidation = new FieldReflectionUtils<FileMetaData>().validateStringFields(file);

			if (!stringFieldsValidation.isValid()) {
				return stringFieldsValidation;
			}
		}

		return new ValidationResponse(true, "validation successful");
	}
}
