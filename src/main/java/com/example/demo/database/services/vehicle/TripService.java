package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.vehicle.Trip;
import com.example.demo.database.repositories.vehicle.TripRepository;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
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
				return new ValidationResponse(false, "ID parameter is required");
			}

			Trip tripFromDatabase = getById(trip.getId());

			if (tripFromDatabase == null) {
				return new ValidationResponse(false, "ID parameter is invalid");
			}
		}

		if (mapping.equals(Mapping.POST) || mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {
			if (trip.getOrigin() != null) {
				if (trip.getOrigin().length() <= 0) {
					return new ValidationResponse(false, "origin cannot be empty");
				}
			}

			if (trip.getDestination() != null) {
				if (trip.getDestination().length() <= 0) {
					return new ValidationResponse(false, "destination cannot be empty");
				}
			}

			if (trip.getKilometres_driven() != null) {
				if (trip.getKilometres_driven() < 0) {
					return new ValidationResponse(false, "invalid kilometres value");
				}
			}


			// OTHER BASIC VALIDATION
		}

		if (mapping.equals(Mapping.DELETE)) {
			if (trip.getId() == null) {
				return new ValidationResponse(false, "ID parameter is required");
			}
		}

		return new ValidationResponse(true, "validation success");
    }
}
