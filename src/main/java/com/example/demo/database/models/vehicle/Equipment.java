package com.example.demo.database.models.vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition="TEXT")
    private String description;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name="type_id", referencedColumnName = "id")
    private EquipmentType type;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name="vehicle_id", referencedColumnName = "id")
    private Vehicle vehicle;

    @Column
    private Boolean isDeleted = false;
}
