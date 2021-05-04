package com.example.demo.database.services;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.user.User;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Fleet;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.repositories.OrganisationRepository;
import com.example.demo.database.repositories.UserRepository;
import com.example.demo.database.repositories.vehicle.FleetRepository;
import com.example.demo.database.repositories.vehicle.VehicleRepository;
import com.example.demo.utils.FieldReflectionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganisationService {

	private final OrganisationRepository repository;


	private final UserRepository userRepository;
	private final VehicleRepository vehicleRepository;
	private final FleetRepository fleetRepository;


	@Transactional
	public List<Organisation> getAll() {
		return repository.findAll();
	}

	@Transactional
	public Organisation getById(Long id) {
		if (id == null) {
			return null;
		}

		Optional<Organisation> organisation = repository.findById(id);

		if (organisation.isEmpty()) {
			return null;
		}

		return organisation.get();
	}

	@Transactional
	public Organisation save(Organisation organisation) {
		return repository.save(organisation);
	}

	@Transactional
	public void delete(Organisation organisation) {
		if (organisation == null || organisation.getId() == null) {
			return;
		}

		List<User> usersInOrganisation = userRepository.findAllByOrganisationId(organisation.getId());
		usersInOrganisation.forEach(user -> {
			user.setOrganisation(null);
			userRepository.save(user);

//			userRepository.delete(user);
		});

		List<Vehicle> vehiclesInOrganisation = vehicleRepository.findAllByOrganisationId(organisation.getId());
		vehiclesInOrganisation.forEach(vehicle -> {
			vehicle.setOrganisation(null);
			vehicleRepository.save(vehicle);

//			vehicleRepository.delete(vehicle);
		});

		List<Fleet> fleetsInOrganisation = fleetRepository.findAllByOrganisationId(organisation.getId());
		fleetsInOrganisation.forEach(fleet -> {
			fleet.setOrganisation(null);
			fleetRepository.save(fleet);

//			fleetRepository.delete(fleet);
		});

		repository.delete(organisation);
	}

	@Transactional
	public void deleteAll() {
		List<User> users = userRepository.findAll();
		users.forEach(user -> {
			user.setOrganisation(null);
			userRepository.save(user);

//			userRepository.delete(user);
		});

		List<Vehicle> vehicleList = vehicleRepository.findAll();
		vehicleList.forEach(vehicle -> {
			vehicle.setOrganisation(null);
			vehicleRepository.save(vehicle);

//			vehicleRepository.delete(vehicle);
		});

		List<Fleet> fleetList = fleetRepository.findAll();
		fleetList.forEach(fleet -> {
			fleet.setOrganisation(null);
			fleetRepository.save(fleet);

//			fleetRepository.delete(fleet);
		});

		repository.deleteAll();
	}


	public ValidationResponse validate(Organisation organisation, Mapping mapping) {

		if (organisation == null) {
			return new ValidationResponse(false, "provided NULL entity");
		}

		if (mapping.equals(Mapping.POST)) {
			organisation.setId(null);
		}

		if (mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {
			if (organisation.getId() == null) {
				return new ValidationResponse(false, "entity ID parameter is required");
			}

			Organisation organisationFromDatabase = getById(organisation.getId());

			if (organisationFromDatabase == null) {
				return new ValidationResponse(false, "entity ID parameter is invalid");
			}
		}

		if (mapping.equals(Mapping.POST) || mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {
			ValidationResponse stringFieldsValidation = new FieldReflectionUtils<Organisation>().validateStringFields(organisation);

			if (!stringFieldsValidation.isValid()) {
				return stringFieldsValidation;
			}
		}

		if (mapping.equals(Mapping.DELETE)) {
			if (organisation.getId() == null) {
				return new ValidationResponse(false, "entity ID parameter is required");
			}
		}

		return new ValidationResponse(true, "validation success");
	}
}
