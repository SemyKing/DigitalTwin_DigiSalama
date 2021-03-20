package com.example.demo.database.services.vehicle;

import com.example.demo.database.models.vehicle.Equipment;
import com.example.demo.database.models.vehicle.FileDB;
import com.example.demo.database.models.vehicle.Trip;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.repositories.vehicle.EquipmentRepository;
import com.example.demo.database.repositories.vehicle.FileRepository;
import com.example.demo.database.repositories.vehicle.TripRepository;
import com.example.demo.database.repositories.vehicle.VehicleRepository;
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
public class VehicleService {

	private final VehicleRepository vehicleRepository;

	private final EquipmentRepository equipmentRepository;

	private final FileRepository imageRepository;

	private final TripRepository tripRepository;


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

			List<FileDB> fileDBS = imageRepository.findAllByVehicleId(vehicle.getId());
			for (FileDB i : fileDBS) {
				i.setVehicle(null);
				imageRepository.save(i);

//				imageRepository.delete(i);
			}


			List<Trip> trips = tripRepository.findAllByVehicleId(vehicle.getId());
			for (Trip t : trips) {
				t.setVehicle(null);
				tripRepository.save(t);

//				tripRepository.delete(t);
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
//		imageRepository.deleteAll();

		List<FileDB> fileDBS = imageRepository.findAll();
		for (FileDB i : fileDBS) {
			i.setVehicle(null);
			imageRepository.save(i);
		}

		//TODO: MAYBE HARD DELETE INSTEAD OF SET NULL
//		tripRepository.deleteAll();

		List<Trip> trips = tripRepository.findAll();
		for (Trip t : trips) {
			t.setVehicle(null);
			tripRepository.save(t);
		}



		vehicleRepository.deleteAll();
	}


	public ValidationResponse validate(Vehicle vehicle, Mapping mapping) {

		if (vehicle.getName() != null) {
			if (vehicle.getName().length() <= 0) {
				return new ValidationResponse(false, "vehicle name cannot be empty");
			}
		}

		if (vehicle.getVin() != null) {
			if (vehicle.getVin().length() <= 0) {
				return new ValidationResponse(false, "vehicle VIN cannot be empty");
			}
		}

		if (vehicle.getRegistrationNumber() != null) {
			if (vehicle.getRegistrationNumber().length() <= 0) {
				return new ValidationResponse(false, "vehicle Registration Number cannot be empty");
			}
		}

		return new ValidationResponse(true, "validation success");
	}
}
