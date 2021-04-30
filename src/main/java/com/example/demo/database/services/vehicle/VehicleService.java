package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Fleet;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.repositories.OrganisationRepository;
import com.example.demo.database.repositories.vehicle.*;
import com.example.demo.utils.FieldReflectionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class VehicleService {

	private final VehicleRepository vehicleRepository;

	private final DistanceRepository distanceRepository;
	private final EquipmentRepository equipmentRepository;
	private final FileRepository fileRepository;
	private final FleetRepository fleetRepository;
	private final RefuelRepository refuelRepository;
	private final TripRepository tripRepository;
	private final VehicleEventRepository vehicleEventRepository;

	private final OrganisationRepository organisationRepository;


	public List<Vehicle> getAll() {
		return vehicleRepository.findAll();
	}

	public Vehicle getById(Long id) {
		if (id == null) {
			return null;
		}

		Optional<Vehicle> vehicle = vehicleRepository.findById(id);

		if (vehicle.isEmpty()) {
			return null;
		}

		return vehicle.get();
	}

	public List<Vehicle> getVehiclesNotInFleet(Long fleetId) {
		if (fleetId == null) {
			return null;
		}

		Optional<Fleet> fleetFromDatabase = fleetRepository.findById(fleetId);

		if (fleetFromDatabase.isEmpty()) {
			return null;
		}

		List<Vehicle> vehiclesNotInFleet = new ArrayList<>();

		List<Vehicle> allVehicles = getAll();

		for (Vehicle vehicle : allVehicles) {
			if (vehicle.getFleets() == null || !vehicle.getFleets().contains(fleetFromDatabase.get())) {
				vehiclesNotInFleet.add(vehicle);
			}
		}

		return vehiclesNotInFleet;
	}

	public Vehicle save(Vehicle vehicle) {
		return vehicleRepository.save(vehicle);
	}


	public void delete(Vehicle vehicle) {
		if (vehicle == null || vehicle.getId() == null) {
			return;
		}

		// FIRST DELETE/SET NULL ALL ENTITIES THAT HAVE FOREIGN KEY OF CURRENT ENTITY

		distanceRepository.findAllByVehicleId(vehicle.getId()).forEach(distance -> {
			distance.setVehicle(null);
			distanceRepository.save(distance);

//			distanceRepository.delete(distance);
		});

		equipmentRepository.findAllByVehicleId(vehicle.getId()).forEach(equipment -> {
			equipment.setVehicle(null);
			equipmentRepository.save(equipment);

//			equipmentRepository.delete(equipment);
		});

		fileRepository.findAllByVehicleId(vehicle.getId()).forEach(file -> {
			file.setVehicle(null);
			fileRepository.save(file);

//			fileRepository.delete(file);
		});


		System.out.println("delete -> vehicleFleets: " + vehicle.getFleets());

		if (vehicle.getFleets() != null) {
			vehicle.getFleets().forEach(fleet -> {
				fleet.getVehicles().remove(vehicle);
				fleetRepository.save(fleet);
			});
		}

		refuelRepository.findAllByVehicleId(vehicle.getId()).forEach(refuel -> {
			refuel.setVehicle(null);
			refuelRepository.save(refuel);

//			refuelRepository.delete(refuel);
		});

		tripRepository.findAllByVehicleId(vehicle.getId()).forEach(trip -> {
			trip.setVehicle(null);
			tripRepository.save(trip);

//			tripRepository.delete(trip);
		});

		vehicleEventRepository.findAllByVehicleId(vehicle.getId()).forEach(event -> {
			event.setVehicle(null);
			vehicleEventRepository.save(event);

//			eventRepository.delete(event);
		});

		vehicleRepository.delete(vehicle);
	}

	public void deleteAll() {

		// FIRST DELETE/SET NULL ALL ENTITIES THAT HAVE FOREIGN KEY OF CURRENT ENTITY

		distanceRepository.findAll().forEach(distance -> {
			distance.setVehicle(null);
			distanceRepository.save(distance);

//			distanceRepository.delete(distance);
		});

		equipmentRepository.findAll().forEach(equipment -> {
			equipment.setVehicle(null);
			equipmentRepository.save(equipment);

//			equipmentRepository.delete(equipment);
		});

		fileRepository.findAll().forEach(file -> {
			file.setVehicle(null);
			fileRepository.save(file);

//			fileRepository.delete(file);
		});

		fleetRepository.findAll().forEach(fleet -> {
			fleet.getVehicles().clear();
			fleetRepository.save(fleet);

//			fleetRepository.delete(fleet);
		});

		refuelRepository.findAll().forEach(refuel -> {
			refuel.setVehicle(null);
			refuelRepository.save(refuel);

//			refuelRepository.delete(refuel);
		});

		tripRepository.findAll().forEach(trip -> {
			trip.setVehicle(null);
			tripRepository.save(trip);

//			tripRepository.delete(trip);
		});

		vehicleEventRepository.findAll().forEach(event -> {
			event.setVehicle(null);
			vehicleEventRepository.save(event);

//			eventRepository.delete(event);
		});

		vehicleRepository.deleteAll();
	}


	public ValidationResponse validate(Vehicle vehicle, Mapping mapping) {

		if (vehicle == null) {
			return new ValidationResponse(false, "provided NULL entity");
		}

		if (mapping.equals(Mapping.POST)) {
			vehicle.setId(null);
		}

		if (mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {
			if (vehicle.getId() == null) {
				return new ValidationResponse(false, "entity ID parameter is required");
			}

			// CHECK ID
			Vehicle vehicleFromDatabase = getById(vehicle.getId());

			if (vehicleFromDatabase == null) {
				return new ValidationResponse(false, "entity ID parameter is invalid");
			}
		}

		if (mapping.equals(Mapping.POST) || mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {

			if (vehicle.getFleets() != null) {
				Set<Fleet> fleets = new HashSet<>();

				for (Fleet fleet : vehicle.getFleets()) {
					if (fleet.getId() == null) {
						return new ValidationResponse(false, "fleet ID is required: " + fleet);
					}

					Optional<Fleet> fleetFromDatabase = fleetRepository.findById(fleet.getId());

					if (fleetFromDatabase.isEmpty()) {
						return new ValidationResponse(false, "fleet ID is invalid: " + fleet);
					}

					fleets.add(fleetFromDatabase.get());
					fleet.getVehicles().add(vehicle);
				}

				vehicle.setFleets(fleets);
			}


			if (vehicle.getOrganisation() != null) {
				if (vehicle.getOrganisation().getId() == null) {
					return new ValidationResponse(false, "organisation ID is required");
				}

				Optional<Organisation> organisation = organisationRepository.findById(vehicle.getOrganisation().getId());

				if (organisation.isEmpty()) {
					return new ValidationResponse(false, "organisation ID is invalid");
				}

				vehicle.setOrganisation(organisation.get());
			}

			ValidationResponse stringFieldsValidation = new FieldReflectionUtils<Vehicle>().validateStringFields(vehicle);

			if (!stringFieldsValidation.isValid()) {
				return stringFieldsValidation;
			}
		}

		if (mapping.equals(Mapping.DELETE)) {
			if (vehicle.getId() == null) {
				return new ValidationResponse(false, "entity ID parameter is required");
			}
		}

		return new ValidationResponse(true, "validation success");
	}



}
