package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Fleet;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.repositories.OrganisationRepository;
import com.example.demo.database.repositories.vehicle.FleetRepository;
import com.example.demo.database.repositories.vehicle.VehicleRepository;
import com.example.demo.utils.FieldReflectionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class FleetService {

	private final FleetRepository repository;

	private final VehicleRepository vehicleRepository;
	private final OrganisationRepository organisationRepository;


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

	public List<Fleet> getFleetsContainingVehicle(Long vehicleId) {
		if (vehicleId == null) {
			return null;
		}

		Optional<Vehicle> vehicleFromDatabase = vehicleRepository.findById(vehicleId);

		if (vehicleFromDatabase.isEmpty()) {
			return null;
		}

		List<Fleet> fleetsContainingVehicle = new ArrayList<>();

		List<Fleet> allFleets = getAll();

		for (Fleet fleet : allFleets) {
			if (fleet.getVehicles().contains(vehicleFromDatabase.get())) {
				fleetsContainingVehicle.add(fleet);
			}
		}

		return fleetsContainingVehicle;
	}

	public List<Fleet> getFleetsNotContainingVehicle(Long vehicleId) {
		if (vehicleId == null) {
			return null;
		}

		Optional<Vehicle> vehicleFromDatabase = vehicleRepository.findById(vehicleId);

		if (vehicleFromDatabase.isEmpty()) {
			return null;
		}

		List<Fleet> fleetsNotContainingVehicle = new ArrayList<>();

		List<Fleet> allFleets = getAll();

		for (Fleet fleet : allFleets) {
			if (fleet.getVehicles() == null || !fleet.getVehicles().contains(vehicleFromDatabase.get())) {
				fleetsNotContainingVehicle.add(fleet);
			}
		}

		return fleetsNotContainingVehicle;
	}

	public Fleet save(Fleet fleet) {
		return repository.save(fleet);
	}

	public void delete(Fleet fleet) {
		if (fleet == null || fleet.getId() == null) {
			return;
		}

		System.out.println("delete -> fleetVehicles: " + fleet.getVehicles());

		// FIRST DELETE/SET NULL ALL ENTITIES THAT HAVE FOREIGN KEY OF CURRENT ENTITY
		if (fleet.getVehicles() != null) {
			fleet.getVehicles().forEach(vehicle -> {
				vehicle.getFleets().remove(fleet);
				vehicleRepository.save(vehicle);

//				vehicleRepository.delete(vehicle);
			});
		}

		repository.delete(fleet);
	}

	public void deleteAll() {

		// FIRST DELETE/SET NULL ALL ENTITIES THAT HAVE FOREIGN KEY OF CURRENT ENTITY

		vehicleRepository.findAll().forEach(vehicle -> {
			vehicle.getFleets().clear();
			vehicleRepository.save(vehicle);

//			vehicleRepository.delete(vehicle);
		});

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
				return new ValidationResponse(false, "entity ID parameter is required");
			}

			Fleet fleetFromDatabase = getById(fleet.getId());

			if (fleetFromDatabase == null) {
				return new ValidationResponse(false, "entity ID parameter is invalid");
			}
		}

		if (mapping.equals(Mapping.POST) || mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {

			if (fleet.getVehicles() != null) {
				Set<Vehicle> vehicles = new HashSet<>();

				for (Vehicle vehicle : fleet.getVehicles()) {
					if (vehicle.getId() == null) {
						return new ValidationResponse(false, "vehicle ID is required: " + vehicle);
					}

					Optional<Vehicle> vehicleFromDatabase = vehicleRepository.findById(vehicle.getId());

					if (vehicleFromDatabase.isEmpty()) {
						return new ValidationResponse(false, "vehicle ID is invalid: " + vehicle);
					}

					vehicles.add(vehicleFromDatabase.get());
					vehicle.getFleets().add(fleet);
				}

				fleet.setVehicles(vehicles);
			}


			if (fleet.getOrganisation() != null) {
				if (fleet.getOrganisation().getId() == null) {
					return new ValidationResponse(false, "organisation ID is required");
				}

				Optional<Organisation> organisation = organisationRepository.findById(fleet.getOrganisation().getId());

				if (organisation.isEmpty()) {
					return new ValidationResponse(false, "organisation ID is invalid: " + fleet.getOrganisation());
				}

				fleet.setOrganisation(organisation.get());
			}


			ValidationResponse stringFieldsValidation = new FieldReflectionUtils<Fleet>().validateStringFields(fleet);

			if (!stringFieldsValidation.isValid()) {
				return stringFieldsValidation;
			}
		}

		if (mapping.equals(Mapping.DELETE)) {
			if (fleet.getId() == null) {
				return new ValidationResponse(false, "entity ID parameter is required");
			}
		}

		return new ValidationResponse(true, "validation success");
	}
}