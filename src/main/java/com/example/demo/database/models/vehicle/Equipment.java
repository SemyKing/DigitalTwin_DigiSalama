package com.example.demo.database.models.vehicle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    @JoinColumn(name="equipment_type_id", referencedColumnName = "id")
    private EquipmentType equipment_type;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name="vehicle_id", referencedColumnName = "id")
    @ToString.Exclude
    private Vehicle vehicle;

    @Transient
    @JsonIgnore
    @ToString.Include
    private Long vehicle_id() {
        return this.vehicle == null ? null : this.vehicle.getId();
    }
}
