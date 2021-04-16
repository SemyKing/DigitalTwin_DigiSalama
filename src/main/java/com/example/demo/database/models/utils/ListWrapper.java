package com.example.demo.database.models.utils;

import com.example.demo.database.models.vehicle.Fleet;
import com.example.demo.database.models.vehicle.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;

@Data
@AllArgsConstructor
/*  List wrapper class for getting list of selected vehicles and fleets
 *  Thymeleaf cannot return List, only a single Object */
public class ListWrapper {

    private ArrayList<Vehicle> vehicles;
    private ArrayList<Fleet> fleets;

    public ListWrapper() {
        vehicles = new ArrayList<>();
        fleets = new ArrayList<>();
    }
}
