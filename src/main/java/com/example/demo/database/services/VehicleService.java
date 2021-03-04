package com.example.demo.database.services;

import com.example.demo.database.models.Vehicle;
import com.example.demo.database.repositories.VehicleRepository;
import com.example.demo.utils.ValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleService {

	private final VehicleRepository repository;


	public List<Vehicle> getAll() {
		return repository.findAll();
	}

	public Vehicle getById(Long id) {
		if (id == null) {
			return null;
		}

		Optional<Vehicle> vehicle = repository.findById(id);

		if (vehicle.isEmpty()) {
			return null;
		}

		return vehicle.get();
	}

	public Vehicle save(Vehicle vehicle) {
		return repository.save(vehicle);
	}

	public void delete(Vehicle vehicle) {
		repository.delete(vehicle);
	}

	public void deleteAll() {
		repository.deleteAll();
	}


	public ValidationResponse validateVehicle(Vehicle vehicle) {

		if (vehicle.getName() != null) {
			if (vehicle.getName().length() <= 0) {
				return new ValidationResponse(null, null, null, vehicle, false, "vehicle name cannot be empty");
			}
		}

		if (vehicle.getVin() != null) {
			if (vehicle.getVin().length() <= 0) {
				return new ValidationResponse(null, null, null, vehicle, false, "vehicle VIN cannot be empty");
			}
		}

		if (vehicle.getRegistrationPlate() != null) {
			if (vehicle.getRegistrationPlate().length() <= 0) {
				return new ValidationResponse(null, null, null, vehicle, false, "vehicle registration plate cannot be empty");
			}
		}

		return new ValidationResponse(null, null, null, vehicle, true, "validation success");
	}


}
