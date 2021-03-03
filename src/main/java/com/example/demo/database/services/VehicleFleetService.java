package com.example.demo.database.services;

import com.example.demo.database.models.VehicleFleet;
import com.example.demo.database.repositories.VehicleFleetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleFleetService {

	private final VehicleFleetRepository repository;


	public List<VehicleFleet> getAll() {
		return repository.findAll();
	}

	public VehicleFleet getById(Long id) {
		if (id == null) {
			return null;
		}

		Optional<VehicleFleet> fleet = repository.findById(id);
		if (fleet.isEmpty()) {
			return null;
		}
		return fleet.get();
	}

	public VehicleFleet save(VehicleFleet fleet) {
		return repository.save(fleet);
	}

	public void delete(VehicleFleet fleet) {
		repository.delete(fleet);
	}

	public void deleteAll() {
		repository.deleteAll();
	}
}
