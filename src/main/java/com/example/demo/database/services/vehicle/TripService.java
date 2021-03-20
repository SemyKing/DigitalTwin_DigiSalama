package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.vehicle.Trip;
import com.example.demo.database.repositories.vehicle.TripRepository;
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
		repository.delete(trip);
	}

	public void deleteAll() {
		repository.deleteAll();
	}
}
