package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Distance;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.repositories.vehicle.DistanceRepository;
import com.example.demo.database.repositories.vehicle.VehicleRepository;
import com.example.demo.utils.FieldReflectionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DistanceService {

	private final DistanceRepository repository;

	private final VehicleRepository vehicleRepository;


	public List<Distance> getAll() {
		return repository.findAll();
	}

	public Distance getById(Long id) {
		if (id == null) {
			return null;
		}

		Optional<Distance> refuel = repository.findById(id);
		if (refuel.isEmpty()) {
			return null;
		}
		return refuel.get();
	}

	public List<Distance> getAllByVehicleId(Long id) {
		if (id == null) {
			return null;
		}

		return repository.findAllByVehicleId(id);
	}

	public Distance getLatestByVehicleId(Long id) {
		List<Distance> distances = getAllByVehicleId(id);

		if (distances.size() > 0) {
			return distances.get(0);
		}

		return null;
	}

	@Transactional
	public Distance save(Distance refuel) {
		return repository.save(refuel);
	}

	@Transactional
	public void delete(Distance refuel) {
		if (refuel == null) {
			return;
		}

		repository.delete(refuel);
	}

	@Transactional
	public void deleteAll() {
		repository.deleteAll();
	}


    public ValidationResponse validate(Distance distance, Mapping mapping) {

		if (distance == null) {
			return new ValidationResponse(false, "provided NULL entity");
		}

		if (mapping.equals(Mapping.POST)) {
			distance.setId(null);
		}

		if (mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {
			if (distance.getId() == null) {
				return new ValidationResponse(false, "entity ID parameter is required");
			}

			Distance distanceFromDatabase = getById(distance.getId());

			if (distanceFromDatabase == null) {
				return new ValidationResponse(false, "entity ID parameter is invalid");
			}
		}

		if (mapping.equals(Mapping.POST) || mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {

			if (distance.getKilometres() == null) {
				return new ValidationResponse(false, "kilometres value is required");
			}

			if (distance.getKilometres() <= 0) {
				return new ValidationResponse(false, "invalid kilometres value: '" + distance.getKilometres() + "'");
			}

			if (distance.getVehicle() == null) {
				return new ValidationResponse(false, "vehicle is required");
			}

			if (distance.getVehicle().getId() == null) {
				return new ValidationResponse(false, "vehicle ID is required");
			}

			Optional<Vehicle> vehicle = vehicleRepository.findById(distance.getVehicle().getId());

			if (vehicle.isEmpty()) {
				return new ValidationResponse(false, "vehicle ID is invalid: " + distance.getVehicle());
			}

			distance.setVehicle(vehicle.get());


			Distance latestDistanceForVehicleFromDatabase = getLatestByVehicleId(distance.getVehicle().getId());

			if (latestDistanceForVehicleFromDatabase != null) {
				if (distance.getKilometres() < latestDistanceForVehicleFromDatabase.getKilometres()) {
					return new ValidationResponse(false, "kilometres value: '" + distance.getKilometres() + "' cannot be lower than previous value: '" + latestDistanceForVehicleFromDatabase.getKilometres() + "'");
				}
			}

			ValidationResponse stringFieldsValidation = new FieldReflectionUtils<Distance>().validateStringFields(distance);

			if (!stringFieldsValidation.isValid()) {
				return stringFieldsValidation;
			}
		}

		if (mapping.equals(Mapping.DELETE)) {
			if (distance.getId() == null) {
				return new ValidationResponse(false, "entity ID parameter is required");
			}

			Distance distanceFromDatabase = getById(distance.getId());

			if (distanceFromDatabase == null) {
				return new ValidationResponse(false, "entity ID parameter is invalid");
			}
		}

		return new ValidationResponse(true, "validation success");
    }
}
