package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Distance;
import com.example.demo.database.models.vehicle.VehicleEvent;
import com.example.demo.database.repositories.vehicle.DistanceRepository;
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
public class DistanceService {

	private final DistanceRepository repository;


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

	public Distance save(Distance refuel) {
		return repository.save(refuel);
	}

	public void delete(Distance refuel) {
		if (refuel == null) {
			return;
		}

		repository.delete(refuel);
	}

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
				return new ValidationResponse(false, "ID parameter is required");
			}

			Distance distanceFromDatabase = getById(distance.getId());

			if (distanceFromDatabase == null) {
				return new ValidationResponse(false, "ID parameter is invalid");
			}
		}

		if (mapping.equals(Mapping.POST) || mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {

			// EMPTY STRING CHECK
			List<Field> stringFields = new ArrayList<>();

			Field[] allFields = Distance.class.getDeclaredFields();
			for (Field field : allFields) {
				if (field.getType().equals(String.class)) {
					stringFields.add(field);
				}
			}

			for (Field field : stringFields) {
				field.setAccessible(true);
				Object object = ReflectionUtils.getField(field, distance);

				if (object != null) {
					if (object instanceof String) {
						if (((String) object).length() <= 0) {
							return new ValidationResponse(false, "'" + field.getName() + "' cannot be empty");
						}
					}
				}
			}

			if (distance.getKilometres() == null) {
				return new ValidationResponse(false, "kilometres value is required");
			}

			if (distance.getKilometres() <= 0) {
				return new ValidationResponse(false, "invalid kilometres value: '" + distance.getKilometres() + "'");
			}

			if (distance.getVehicle() == null) {
				return new ValidationResponse(false, "vehicle is required");
			}

			Distance latestDistanceForVehicleFromDatabase = getLatestByVehicleId(distance.getVehicle().getId());

			if (latestDistanceForVehicleFromDatabase != null) {

				System.out.println("DISTANCE FROM DB: " + latestDistanceForVehicleFromDatabase);

				if (distance.getKilometres() < latestDistanceForVehicleFromDatabase.getKilometres()) {
					return new ValidationResponse(false, "kilometres value: '" + distance.getKilometres() + "' cannot be lower than previous value: '" + latestDistanceForVehicleFromDatabase.getKilometres() + "'");
				}
			}
		}

		if (mapping.equals(Mapping.DELETE)) {
			if (distance.getId() == null) {
				return new ValidationResponse(false, "ID parameter is required");
			}

			Distance distanceFromDatabase = getById(distance.getId());

			if (distanceFromDatabase == null) {
				return new ValidationResponse(false, "ID parameter is invalid");
			}
		}

		return new ValidationResponse(true, "validation success");
    }
}
