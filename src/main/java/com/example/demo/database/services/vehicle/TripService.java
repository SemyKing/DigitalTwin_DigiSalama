package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Trip;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.repositories.vehicle.TripRepository;
import com.example.demo.database.repositories.vehicle.VehicleRepository;
import com.example.demo.utils.FieldReflectionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TripService {

	private final TripRepository repository;

	private final VehicleRepository vehicleRepository;


	public List<Trip> getAll() {
		return repository.findAll();
	}

	public Trip getById(Long id) {
		if (id == null) {
			return null;
		}

		Optional<Trip> trip = repository.findById(id);
		if (trip.isEmpty()) {
			return null;
		}
		return trip.get();
	}

	public List<Trip> getAllByVehicleId(Long id) {
		if (id == null) {
			return null;
		}

		return repository.findAllByVehicleId(id);
	}

	public Trip save(Trip trip) {
		return repository.save(trip);
	}

	public void delete(Trip trip) {
		if (trip == null) {
			return;
		}

		if (trip.getId() == null) {
			return;
		}

		repository.delete(trip);
	}

	public void deleteAll() {
		repository.deleteAll();
	}


    public ValidationResponse validate(Trip trip, Mapping mapping) {

		if (trip == null) {
			return new ValidationResponse(false, "provided NULL entity");
		}

		if (mapping.equals(Mapping.POST)) {
			trip.setId(null);
		}

		if (mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {
			if (trip.getId() == null) {
				return new ValidationResponse(false, "entity ID parameter is required");
			}

			Trip tripFromDatabase = getById(trip.getId());

			if (tripFromDatabase == null) {
				return new ValidationResponse(false, "entity ID parameter is invalid");
			}
		}

		if (mapping.equals(Mapping.POST) || mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {

			if (trip.getVehicle() == null) {
				return new ValidationResponse(false, "vehicle is required");
			}

			if (trip.getVehicle().getId() == null) {
				return new ValidationResponse(false, "vehicle ID is required");
			}

			Optional<Vehicle> vehicle = vehicleRepository.findById(trip.getVehicle().getId());

			if (vehicle.isEmpty()) {
				return new ValidationResponse(false, "vehicle ID is invalid");
			}

			trip.setVehicle(vehicle.get());


			ValidationResponse stringFieldsValidation = new FieldReflectionUtils<Trip>().validateStringFields(trip);

			if (!stringFieldsValidation.isValid()) {
				return stringFieldsValidation;
			}
		}

		if (mapping.equals(Mapping.DELETE)) {
			if (trip.getId() == null) {
				return new ValidationResponse(false, "entity ID parameter is required");
			}
		}

		return new ValidationResponse(true, "validation success");
    }
}
