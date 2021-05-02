package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.FileDB;
import com.example.demo.database.models.vehicle.Refuel;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.models.vehicle.VehicleEvent;
import com.example.demo.database.repositories.vehicle.FileRepository;
import com.example.demo.database.repositories.vehicle.RefuelRepository;
import com.example.demo.database.repositories.vehicle.VehicleEventRepository;
import com.example.demo.database.repositories.vehicle.VehicleRepository;
import com.example.demo.utils.FieldReflectionUtils;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileService {

	private final FileRepository repository;

	private final VehicleRepository vehicleRepository;
	private final RefuelRepository refuelRepository;
	private final VehicleEventRepository vehicleEventRepository;


	public List<FileDB> getAll() {
		return repository.findAll();
	}

	public FileDB getById(Long id) {
		if (id == null) {
			return null;
		}

		Optional<FileDB> image = repository.findById(id);
		if (image.isEmpty()) {
			return null;
		}
		return image.get();
	}

	public List<FileDB> getAllByVehicleId(Long id) {
		if (id == null) {
			return null;
		}

		return repository.findAllByVehicleId(id);
	}

	public List<FileDB> getAllByRefuelId(Long id) {
		if (id == null) {
			return null;
		}

		return repository.findAllByRefuelId(id);
	}

	@Transactional
	public FileDB save(FileDB file) {
		return repository.save(file);
	}

	@Transactional
	public FileDB save(MultipartFile file) throws IOException {
		String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

		if (fileName.length() <= 0) {
			throw new FileUploadException("File name cannot be empty");
		}

		if (file.getContentType() == null) {
			throw new FileUploadException("File content type cannot be NULL");
		}

		FileDB fileDB = new FileDB(fileName, file.getContentType(), file.getBytes());

		return repository.save(fileDB);
	}

	@Transactional
	public void delete(FileDB fileDB) {
		if (fileDB == null) {
			return;
		}

		if (fileDB.getId() == null) {
			return;
		}
		repository.delete(fileDB);
	}

	@Transactional
	public void deleteAll() {
		repository.deleteAll();
	}


	public String getImageData(byte[] byteData) {
		return Base64.getMimeEncoder().encodeToString(byteData);
	}

	public ValidationResponse validate(FileDB file, Mapping mapping) {

		if (file == null) {
			return new ValidationResponse(false, "provided NULL entity");
		}

//		if (mapping.equals(Mapping.POST)) {
//			file.setId(null);
//
//			if (file.getMultipart_file() == null) {
//				return new ValidationResponse(false, "multipart_file is required");
//			}
//		}

		if (mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {
			if (file.getId() == null) {
				return new ValidationResponse(false, "entity ID parameter is required");
			}

			FileDB fileFromDatabase = getById(file.getId());

			if (fileFromDatabase == null) {
				return new ValidationResponse(false, "entity ID parameter is invalid");
			}
		}

		if (mapping.equals(Mapping.POST) || mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {

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


			ValidationResponse stringFieldsValidation = new FieldReflectionUtils<FileDB>().validateStringFields(file);

			if (!stringFieldsValidation.isValid()) {
				return stringFieldsValidation;
			}
		}

		return new ValidationResponse(true, "validation successful");
	}
}
