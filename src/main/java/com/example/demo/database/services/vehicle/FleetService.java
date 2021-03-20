package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.vehicle.Fleet;
import com.example.demo.database.repositories.vehicle.FleetRepository;
import com.example.demo.utils.Mapping;
import com.example.demo.utils.ValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class FleetService {

	private final FleetRepository repository;


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

	public Fleet save(Fleet fleet) {
		return repository.save(fleet);
	}

	public void delete(Fleet fleet) {
		repository.delete(fleet);
	}

	public void deleteAll() {
		repository.deleteAll();
	}

	public ValidationResponse validate(Fleet fleet, Mapping mapping) {

		if (fleet.getName() != null) {
			if (fleet.getName().length() <= 0) {
				return new ValidationResponse(false, "name cannot be empty");
			}
		}

		return new ValidationResponse(true, "validation success");
	}
}