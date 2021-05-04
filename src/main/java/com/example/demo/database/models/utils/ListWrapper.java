package com.example.demo.database.models.utils;

import com.example.demo.database.models.vehicle.FileMetaData;
import com.example.demo.database.models.vehicle.Fleet;
import com.example.demo.database.models.vehicle.Vehicle;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
/*  List wrapper class for getting list of selected vehicles, fleets, files etc...
 *  Thymeleaf cannot return List, only a single Object */
public class ListWrapper {

    private ArrayList<Vehicle> vehicles = new ArrayList<>();
    private ArrayList<Fleet> fleets = new ArrayList<>();
    private ArrayList<FileMetaData> files = new ArrayList<>();
}
