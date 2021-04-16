package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Fleet;
import com.example.demo.database.models.vehicle.Trip;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.repositories.vehicle.FleetRepository;
import com.example.demo.database.repositories.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class FleetService {

	private final FleetRepository repository;

	private final VehicleRepository vehicleRepository;


	public List<Fleet> getAll() {
		return repository.findAll();
	}

	public Fleet getById(Long id) {
		if (id == null) {
			return null;
		}

		Optional<Fleet> fleet = repository.findById(id);
		if (fleet.isEmpty()) {
			return null;
		}
		return fleet.get();
	}

	public List<Fleet> getFleetsNotContainingVehicle(Long vehicleId) {
		if (vehicleId == null) {
			return null;
		}

		Optional<Vehicle> vehicleFromDatabase = vehicleRepository.findById(vehicleId);

		if (vehicleFromDatabase.isEmpty()) {
			return null;
		}

		List<Fleet> fleetsNotContainingVehicle = new ArrayList<>();

		List<Fleet> allFleets = getAll();

		for (Fleet fleet : allFleets) {
			if (fleet.getVehicles() == null || !fleet.getVehicles().contains(vehicleFromDatabase.get())) {
				fleetsNotContainingVehicle.add(fleet);
			}
		}

		return fleetsNotContainingVehicle;
	}

	public Fleet save(Fleet fleet) {
		return repository.save(fleet);
	}

	public void delete(Fleet fleet) {
		if (fleet == null) {
			return;
		}

		if (fleet.getId() == null) {
			return;
		}
		repository.delete(fleet);
	}

	public void deleteAll() {
		repository.deleteAll();
	}

	public ValidationResponse validate(Fleet fleet, Mapping mapping) {

		if (fleet == null) {
			return new ValidationResponse(false, "provided NULL entity");
		}

		if (mapping.equals(Mapping.POST)) {
			fleet.setId(null);
		}

		if (mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {
			if (fleet.getId() == null) {
				return new ValidationResponse(false, "ID parameter is required");
			}

			Fleet fleetFromDatabase = getById(fleet.getId());

			if (fleetFromDatabase == null) {
				return new ValidationResponse(false, "ID parameter is invalid");
			}
		}

		if (mapping.equals(Mapping.POST) || mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {

			// EMPTY STRING CHECK
			List<Field> stringFields = new ArrayList<>();

			Field[] allFields = Fleet.class.getDeclaredFields();
			for (Field field : allFields) {
				if (field.getType().equals(String.class)) {
					stringFields.add(field);
				}
			}

			for (Field field : stringFields) {
				field.setAccessible(true);
				Object object = ReflectionUtils.getField(field, fleet);

				if (object != null) {
					if (object instanceof String) {
						if (((String) object).length() <= 0) {
							return new ValidationResponse(false, "'" + field.getName() + "' cannot be empty");
						}
					}
				}
			}
		}

		if (mapping.equals(Mapping.DELETE)) {
			if (fleet.getId() == null) {
				return new ValidationResponse(false, "ID parameter is required");
			}
		}

		return new ValidationResponse(true, "validation success");
	}
}