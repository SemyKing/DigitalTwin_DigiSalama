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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DistanceService {

	private final DistanceRepository repository;

	private final VehicleRepository vehicleRepository;


	@Transactional
	public List<Distance> getAll() {
		return repository.findAll();
	}

	@Transactional
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

	@Transactional
	public List<Distance> getAllByVehicleId(Long id) {
		if (id == null) {
			return null;
		}

		return repository.findAllByVehicleId(id);
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


	public boolean distanceWithSameTimestampAlreadyExists(Distance distance) {
		List<Distance> vehicleDistances = getAllByVehicleId(distance.getVehicle().getId());

		for (Distance d : vehicleDistances) {
			if (d.getTimestamp().equals(distance.getTimestamp())) {
				return true;
			}
		}

		return false;
	}

	public ValidationResponse checkDistanceDateAndKilometres(Distance distanceToCheck) {
		List<Distance> vehicleDistances = new ArrayList<>(getAllByVehicleId(distanceToCheck.getVehicle().getId()));

		// REMOVE DISTANCE IF ALREADY THERE (PATCH, PUT)
		if (distanceToCheck.getId() != null) {
			vehicleDistances.removeIf(distanceToRemove -> distanceToRemove.getId().equals(distanceToCheck.getId()));
		}

		if (vehicleDistances.size() == 0) {
			return new ValidationResponse(true, "");
		}

		if (vehicleDistances.size() == 1) {
			if ((vehicleDistances.get(0).getTimestamp().isBefore(distanceToCheck.getTimestamp()) && vehicleDistances.get(0).getKilometres() < distanceToCheck.getKilometres()) ||
					(vehicleDistances.get(0).getTimestamp().isAfter(distanceToCheck.getTimestamp()) && vehicleDistances.get(0).getKilometres() > distanceToCheck.getKilometres())) {
				return new ValidationResponse(true, "");
			}
		}

		vehicleDistances.add(distanceToCheck);

		// SORT BY TIMESTAMP FROM NEWEST TO OLDEST
		vehicleDistances.sort(Comparator.comparing(Distance::getTimestamp).reversed());

		// CHECK SURROUNDING DISTANCES
		int leftDistanceIndex = -1;
		int rightDistanceIndex = -1;
		int distanceIndex = vehicleDistances.indexOf(distanceToCheck);

		leftDistanceIndex = distanceIndex - 1;
		rightDistanceIndex = distanceIndex + 1;

		if (leftDistanceIndex >= 0) {
			Distance leftDistance = vehicleDistances.get(leftDistanceIndex);

			if (leftDistance.getKilometres() < distanceToCheck.getKilometres()) {
				return new ValidationResponse(false, "invalid kilometres value, distance: '" + distanceToCheck + "' cannot be before: '" + leftDistance + "'");
			}
		}

		if (rightDistanceIndex < vehicleDistances.size()) {
			Distance rightDistance = vehicleDistances.get(rightDistanceIndex);

			if (rightDistance.getKilometres() > distanceToCheck.getKilometres()) {
				return new ValidationResponse(false, "invalid kilometres value, distance: '" + distanceToCheck + "' cannot be after: '" + rightDistance + "'");
			}
		}

		return new ValidationResponse(true, "");
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


			if (distance.getTimestamp() == null) {
				return new ValidationResponse(false, "timestamp value is required");
			}

			System.out.println("VALIDATION DISTANCE: \n" + distance);


			if (distanceWithSameTimestampAlreadyExists(distance)) {
				return new ValidationResponse(false, "distance with the same timestamp already exists");
			}

			ValidationResponse distanceValidation = checkDistanceDateAndKilometres(distance);

			if (!distanceValidation.isValid()) {
				return distanceValidation;
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
