package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.FileMetaData;
import com.example.demo.database.models.vehicle.Refuel;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.repositories.vehicle.FileMetaDataRepository;
import com.example.demo.database.repositories.vehicle.RefuelRepository;
import com.example.demo.database.repositories.vehicle.VehicleRepository;
import com.example.demo.utils.FieldReflectionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefuelService {

	private final RefuelRepository repository;

	private final FileMetaDataRepository fileMetaDataRepository;
	private final VehicleRepository vehicleRepository;


	@Transactional
	public List<Refuel> getAll() {
		return repository.findAll();
	}

	@Transactional
	public Refuel getById(Long id) {
		if (id == null) {
			return null;
		}

		Optional<Refuel> refuel = repository.findById(id);
		if (refuel.isEmpty()) {
			return null;
		}
		return refuel.get();
	}

	@Transactional
	public Refuel save(Refuel refuel) {
		return repository.save(refuel);
	}

	@Transactional
	public void delete(Refuel refuel) {
		if (refuel == null || refuel.getId() == null) {
			return;
		}

		// FIRST DELETE/SET NULL ALL ENTITIES THAT HAVE FOREIGN KEY OF CURRENT ENTITY

		fileMetaDataRepository.findAllByRefuelId(refuel.getId()).forEach(file -> {
			file.setRefuel(null);
			fileMetaDataRepository.save(file);

//			fileRepository.delete(file);
		});

		repository.delete(refuel);
	}

	@Transactional
	public void deleteAll() {

		// FIRST DELETE/SET NULL ALL ENTITIES THAT HAVE FOREIGN KEY OF CURRENT ENTITY

		fileMetaDataRepository.findAll().forEach(file -> {
			file.setRefuel(null);
			fileMetaDataRepository.save(file);

//			fileRepository.delete(file);
		});

		repository.deleteAll();
	}


    public ValidationResponse validate(Refuel refuel, Mapping mapping) {

		if (refuel == null) {
			return new ValidationResponse(false, "provided NULL entity");
		}

		if (mapping.equals(Mapping.POST)) {
			refuel.setId(null);
		}

		if (mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {
			if (refuel.getId() == null) {
				return new ValidationResponse(false, "entity ID parameter is required");
			}

			Refuel refuelFromDatabase = getById(refuel.getId());

			if (refuelFromDatabase == null) {
				return new ValidationResponse(false, "entity ID parameter is invalid");
			}
		}

		if (mapping.equals(Mapping.POST) || mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {

			if (refuel.getVehicle() == null) {
				return new ValidationResponse(false, "vehicle is required");
			}

			if (refuel.getVehicle().getId() == null) {
				return new ValidationResponse(false, "vehicle ID is required");
			}

			Optional<Vehicle> vehicle = vehicleRepository.findById(refuel.getVehicle().getId());

			if (vehicle.isEmpty()) {
				return new ValidationResponse(false, "vehicle ID is invalid");
			}

			refuel.setVehicle(vehicle.get());


			if (refuel.getFile() != null && refuel.getFile().getId() != null) {
				Optional<FileMetaData> fileFromDatabase = fileMetaDataRepository.findById(refuel.getFile().getId());

				if (fileFromDatabase.isEmpty()) {
					return new ValidationResponse(false, "selected file not found");
				}

				fileFromDatabase.get().setRefuel(refuel);
			}


			ValidationResponse stringFieldsValidation = new FieldReflectionUtils<Refuel>().validateStringFields(refuel);

			if (!stringFieldsValidation.isValid()) {
				return stringFieldsValidation;
			}
		}

		if (mapping.equals(Mapping.DELETE)) {
			if (refuel.getId() == null) {
				return new ValidationResponse(false, "entity ID parameter is required");
			}

			Refuel refuelFromDatabase = getById(refuel.getId());

			if (refuelFromDatabase == null) {
				return new ValidationResponse(false, "entity ID parameter is invalid");
			}
		}

		return new ValidationResponse(true, "validation success");
    }
}
