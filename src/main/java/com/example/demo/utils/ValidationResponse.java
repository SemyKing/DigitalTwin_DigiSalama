package com.example.demo.utils;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.User;
import com.example.demo.database.models.Vehicle;
import com.example.demo.database.models.VehicleFleet;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationResponse {

    private Organisation organisation;
    private User user;
    private VehicleFleet vehicleFleet;
    private Vehicle vehicle;

    private boolean valid;
    private String message;

}
