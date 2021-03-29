package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.vehicle.Fleet;
import com.example.demo.database.repositories.vehicle.FleetRepository;
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
			if (fleet.getName() != null) {
				if (fleet.getName().length() <= 0) {
					return new ValidationResponse(false, "name cannot be empty");
				}
			}


			// OTHER VALIDATION
		}

		if (mapping.equals(Mapping.DELETE)) {
			if (fleet.getId() == null) {
				return new ValidationResponse(false, "ID parameter is required");
			}
		}

		return new ValidationResponse(true, "validation success");
	}
}