package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.vehicle.*;
import com.example.demo.database.repositories.vehicle.*;
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
public class VehicleService {

	private final VehicleRepository vehicleRepository;

	private final EquipmentRepository equipmentRepository;

	private final FileRepository fileRepository;

	private final TripRepository tripRepository;

	private final RefuelRepository refuelRepository;


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

	public List<Vehicle> getAllByFleetId(Long id) {
		if (id == null) {
			return null;
		}

		return vehicleRepository.findAllByFleetId(id);
	}

	public Vehicle save(Vehicle vehicle) {
		return vehicleRepository.save(vehicle);
	}


	public void delete(Vehicle vehicle) {
		if (vehicle == null) {
			return;
		}

		// FIRST DELETE/SET NULL ALL ENTITIES THAT HAVE vehicle_id FOREIGN KEY

		if (vehicle.getId() != null) {
			List<Equipment> equipment = equipmentRepository.findAllByVehicleId(vehicle.getId());
			for (Equipment e : equipment) {
				e.setVehicle(null);
				equipmentRepository.save(e);

//				equipmentRepository.delete(e);
			}

			List<Trip> trips = tripRepository.findAllByVehicleId(vehicle.getId());
			for (Trip trip : trips) {
				trip.setVehicle(null);
				tripRepository.save(trip);

//				tripRepository.delete(trip);
			}

			List<FileDB> files = fileRepository.findAllByVehicleId(vehicle.getId());
			for (FileDB file : files) {
				file.setVehicle(null);
				fileRepository.save(file);

//				fileRepository.delete(file);
			}

			List<Refuel> refuels = refuelRepository.findAllByVehicleId(vehicle.getId());
			for (Refuel refuel : refuels) {
				refuel.setVehicle(null);
				refuelRepository.save(refuel);

//				refuelRepository.delete(refuel);
			}



			vehicleRepository.delete(vehicle);
		}
	}

	public void deleteAll() {

		// FIRST DELETE/SET NULL ALL ENTITIES THAT HAVE vehicle_id FOREIGN KEY

		//TODO: MAYBE HARD DELETE INSTEAD OF SET NULL
//		equipmentRepository.deleteAll();

		List<Equipment> equipment = equipmentRepository.findAll();
		for (Equipment e : equipment) {
			e.setVehicle(null);
			equipmentRepository.save(e);
		}

		//TODO: MAYBE HARD DELETE INSTEAD OF SET NULL
//		tripRepository.deleteAll();

		List<Trip> trips = tripRepository.findAll();
		for (Trip trip : trips) {
			trip.setVehicle(null);
			tripRepository.save(trip);
		}

		//TODO: MAYBE HARD DELETE INSTEAD OF SET NULL
//		fileRepository.deleteAll();

		List<FileDB> files = fileRepository.findAll();
		for (FileDB file : files) {
			file.setVehicle(null);
			fileRepository.save(file);
		}

		//TODO: MAYBE HARD DELETE INSTEAD OF SET NULL
//		refuelRepository.deleteAll();

		List<Refuel> refuels = refuelRepository.findAll();
		for (Refuel refuel : refuels) {
			refuel.setVehicle(null);
			refuelRepository.save(refuel);
		}





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
				return new ValidationResponse(false, "ID parameter is required");
			}

			// CHECK ID
			Vehicle vehicleFromDatabase = getById(vehicle.getId());

			if (vehicleFromDatabase == null) {
				return new ValidationResponse(false, "ID parameter is invalid");
			}
		}

		if (mapping.equals(Mapping.POST) || mapping.equals(Mapping.PUT) || mapping.equals(Mapping.PATCH)) {
			if (vehicle.getName() != null) {
				if (vehicle.getName().length() <= 0) {
					return new ValidationResponse(false, "name cannot be empty");
				}
			}

			if (vehicle.getVin() != null) {
				if (vehicle.getVin().length() <= 0) {
					return new ValidationResponse(false, "vin cannot be empty");
				}
			}

			if (vehicle.getRegistration_number() != null) {
				if (vehicle.getRegistration_number().length() <= 0) {
					return new ValidationResponse(false, "registration number cannot be empty");
				}
			}
		}

		if (mapping.equals(Mapping.DELETE)) {
			if (vehicle.getId() == null) {
				return new ValidationResponse(false, "ID parameter is required");
			}
		}

		return new ValidationResponse(true, "validation success");
	}
}
