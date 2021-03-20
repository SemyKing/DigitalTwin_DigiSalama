package com.example.demo.database.models.vehicle;

import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Data
/* Vehicle List wrapper class for getting list of selected vehicles in fleet management*/
public class VehicleListWrapper {

    @NonNull
    private List<Vehicle> vehicles;

    public VehicleListWrapper() {
        vehicles = new ArrayList<>();
    }

}
