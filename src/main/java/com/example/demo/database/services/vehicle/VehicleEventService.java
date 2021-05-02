package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.models.vehicle.VehicleEvent;
import com.example.demo.database.repositories.vehicle.FileRepository;
import com.example.demo.database.repositories.vehicle.VehicleEventRepository;
import com.example.demo.database.repositories.vehicle.VehicleRepository;
import com.example.demo.utils.FieldReflectionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleEventService {

	private final VehicleEventRepository repository;

	private final FileRepository fileRepository;
	private final VehicleRepository vehicleRepository;


	public List<VehicleEvent> getAll() {
		return repository.findAll();
	}

	public VehicleEvent getById(Long id) {
		if (id == null) {
			return null;
		}

		Optional<VehicleEvent> event = repository.findById(id);
		if (event.isEmpty()) {
			return null;
		}
		return event.get();
	}

	public List<VehicleEvent> getAllByVehicleId(Long id) {
		if (id == null) {
			return null;
		}

		return repository.findAllByVehicleId(id);
	}

	@Transactional
	public VehicleEvent save(VehicleEvent event) {
		return repository.save(event);
	}

	@Transactional
	public void delete(VehicleEvent event) {
		if (event == null || event.getId() == null) {
			return;
		}

		// FIRST DELETE/SET NULL ALL ENTITIES THAT HAVE FOREIGN KEY OF CURRENT ENTITY

		fileRepository.findAllByVehicleEventId(event.getId()).forEach(file-> {
			file.setVehicle_event(null);
			fileRepository.save(file);

//			fileRepository.delete(file);
		});

		repository.delete(event);
	}

	@Transactional
	public void deleteAll() {

		// FIRST DELETE/SET NULL ALL ENTITIES THAT HAVE FOREIGN KEY OF CURRENT ENTITY

		fileRepository.findAll().forEach(file-> {
			file.setVehicle_event(null);
			fileRepository.save(file);

//			fileRepository.delete(file);
		});

		repository.deleteAll();
	}


    public ValidationResponse validate(VehicleEvent event, Mapping mapping) {

		if (event == null) {
			return new ValidationResponse(false, "provided NULL entity");
		}

		if (mapping.equals(Mapping.POST)) {
			event.setId(null);
		}

		if (mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {
			if (event.getId() == null) {
				return new ValidationResponse(false, "entity ID parameter is required");
			}

			VehicleEvent eventFromDatabase = getById(event.getId());

			if (eventFromDatabase == null) {
				return new ValidationResponse(false, "entity ID parameter is invalid");
			}
		}

		if (mapping.equals(Mapping.POST) || mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {

			if (event.getVehicle() == null) {
				return new ValidationResponse(false, "vehicle is required");
			}

			if (event.getVehicle().getId() == null) {
				return new ValidationResponse(false, "vehicle ID is required");
			}

			Optional<Vehicle> vehicle = vehicleRepository.findById(event.getVehicle().getId());

			if (vehicle.isEmpty()) {
				return new ValidationResponse(false, "vehicle ID is invalid");
			}

			event.setVehicle(vehicle.get());


			ValidationResponse stringFieldsValidation = new FieldReflectionUtils<VehicleEvent>().validateStringFields(event);

			if (!stringFieldsValidation.isValid()) {
				return stringFieldsValidation;
			}
		}

		if (mapping.equals(Mapping.DELETE)) {
			if (event.getId() == null) {
				return new ValidationResponse(false, "entity ID parameter is required");
			}

			VehicleEvent eventFromDatabase = getById(event.getId());

			if (eventFromDatabase == null) {
				return new ValidationResponse(false, "entity ID parameter is invalid");
			}
		}

		return new ValidationResponse(true, "validation success");
    }
}
