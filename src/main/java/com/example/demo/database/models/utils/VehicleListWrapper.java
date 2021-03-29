package com.example.demo.database.models.utils;

import com.example.demo.database.models.vehicle.Vehicle;
import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Data
/* Vehicle List wrapper class for getting list of selected vehicles in fleet management
*  Thymeleaf cannot return List, only a single Object */
public class VehicleListWrapper {

    @NonNull
    private List<Vehicle> vehicles;

    public VehicleListWrapper() {
        vehicles = new ArrayList<>();
    }

}
