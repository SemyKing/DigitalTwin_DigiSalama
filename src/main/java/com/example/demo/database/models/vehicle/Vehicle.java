package com.example.demo.database.models.vehicle;

import com.example.demo.database.models.Organisation;
import com.example.demo.utils.DateUtils;
import com.example.demo.utils.LocalDateTimeConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String name;


	/**
	 * This Set is not visible in toString() nor in JSON GET requests
	 * {@link #fleet_ids} is displayed instead
	 * because this Set and {@link Fleet#vehicles} are in ManyToMany relationship and recursively call each other in toString() and JSON GET
	 *
	 * however this Set must be used in POST, PATCH and PUT requests containing Fleet objects with IDs (other parameters are not necessary)
	 */
	@JsonIgnore
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "vehicle_fleets",
			joinColumns = @JoinColumn(name = "vehicle_id"),
			inverseJoinColumns = @JoinColumn(name = "fleet_id"))
	private Set<Fleet> fleets = new HashSet<>();


	@Transient
	@ToString.Include
	@Getter(AccessLevel.NONE)
	private List<Long> fleet_ids = new ArrayList<>();

	// CUSTOM GETTER
	public List<Long> getFleet_ids() {
		fleet_ids.clear();

		for (Fleet fleet : fleets) {
			fleet_ids.add(fleet.getId());
		}

		Collections.sort(fleet_ids);

		return fleet_ids;
	}

	@ManyToOne
	@JoinColumn(name="organisation_id", referencedColumnName = "id")
	private Organisation organisation;



	// 1+4 paikkaiset normaalit henk.autot, M1-luokka
	//  kotihoito, taksit, VPL/SPL kuljetuksia/tehtäviä, koulukuljetuksia, kelakuljetuksia, terveydenhuollon näytekuljetukset, postin kuljetuksia (pienellä autolla tehtävissä)

	// AND ALL GENERAL / COMMON PARAMETERS

	@Column
	private String required_driving_licence;

	@Column
	private String registration_number;

	@Column
	private String vin;

	@Column
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@Convert(converter = LocalDateTimeConverter.class)
	//Käyttöönottotarkastus suoritettu (pvm)
	private LocalDateTime commissioning_check_performed_date = LocalDateTime.now();

	@Column
	private String purity_marking;

	@Column
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@Convert(converter = LocalDateTimeConverter.class)
	//Ensimmäinen käyttöönottopäivä
	private LocalDateTime first_commissioning_date = LocalDateTime.now();

	@Column
	private String brand;

	@Column
	private String model;

	@Column
	private String color;

	@Column
	//Käyttövoima (hybridi, täyssähkö, uusiutuva diesel, maa-/biokaasu, vety, bensiini, diesel)
	private String propulsion;

	@Column
	//NEDC (g/km) CO2 päästöarvo
	private String nedc;

	@Column
	//WLTP (g/km) CO2 päästöarvo
	private String wltp;

	@Column
	//Yhdistetty kulutus (l/100km tai Wh/km)
	private String combined_vehicle_consumption;

	@Column
	private String euro_emission_class;

	@Column
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@Convert(converter = LocalDateTimeConverter.class)
	private LocalDateTime next_inspection_date = LocalDateTime.now();

	@Column
	private String engine_capacity;

	@Column
	//Teho (kW)
	private String power;

	@Column
	//Vääntö (Nm)
	private String torque;

	@Column
	//Vaihteisto (manuaali/automaatti/CVT)
	private String gearing;

	@Column
	private String traction;

	@Column
	private String turbo;

	@Column
	// Korimalli (pitkä, lyhyt, matala, korkea)
	private String vehicle_body_model;

	@Column
	private String total_amount_of_seats;

	@Column
	private String total_amount_of_doors;

	@Column
	private String total_amount_of_high_voltage_locks;

	@Column
	//Akkukapasitetti (kWh)
	private String battery_capacity;

	@Column
	private String fast_charging_capability;

	@Column
	private String charging_time_80_percent;

	@Column
	private String charging_time_100_percent;

	@Column
	//Toimintamatka täydellä latauksella
	private String full_charge_distance;

	@Column
	private String own_mass;

	@Column
	private String total_mass;

	@Column
	private String towing_mass;

	@Column
	private String height;

	@Column
	private String width;

	@Column
	private String length;

	@Column
	private String tire_size;

	@Column
	private String rim_size;

	@Column
	private String tire_location;

	@Column
	private String tire_speed_class;

	@Column
	private String tire_carrying_capacity;

	@Column
	private String tire_offset;



	// 1+8 paikkaiset tila-autot, M1-luokka (koululaisistuimilla esim. 1+K13)
	// taksit, VPL/SPL kuljetuksia/tehtäviä, koulukuljetuksia, kelakuljetuksia, esi- ja peruskoululaisten erilliskuljetukset, eläkeläisten kuljetukset, postin kuljetuksia (tila-autolla tehtävissä)

	// Koululaisistuinpaikkojen määrä (esim 1+K13 / 1+K12 / 1+7 / 1+8)
	@Column
	private String amount_of_pupil_seats;

	@Column
	private String differential_lock;



	// Pakettiautot N1: Alle 3500 kg
	// Pienet pakettiautot sekä suuret pakettiautot
	// Palveluliikenne (rahti), tieliikenteen postikuljetukset, pakettien kuljetuspalvelut, postin jakelupalvelut, pakettien jakelupalvelut, terveydenhuollon näytekuljetus, huoltoajoneuvo

	// Tavaratilan koko (litraa/kuutioissa)
	@Column
	private String trunk_size;

	@Column
	private String transport_capacity;

	@Column
	private Boolean is_refrigerated_transport = false;




	// Pikkubussit M2: Alle 5000 kg
	// 1+9 alkaen noin 1+16 paikkaisiin (esim MB Sprinter, Iveco Daily, VDL)
	// taksit, VPL/SPL kuljetuksia/tehtäviä, koulukuljetuksia, kelakuljetuksia, esi- ja peruskoululaisten erilliskuljetukset, eläkeläisten kuljetukset, muut tilaus/yksittäiskuljetukset (pikkubussilla tehtävissä, istuinpaikkojen mukaan)

	@Column
	// Moottorin sijainti (etu, keski, taka)
	private String motor_location;

	@Column
	// Kääntösäde (m)
	private String turning_radius;

	@Column
	// Ovien ohjaus (hydrauliset /pneumaattiset)
	private String door_control_type;

	@Column
	private String total_amount_of_axles;

	@Column
	private String total_amount_and_location_of_twin_wheels_axles;

	@Column
	private String total_amount_and_location_of_driving_axles;

	@Column
	private String total_amount_and_location_of_braking_axles;

	@Column
	private String total_amount_and_location_of_steering_axles;



	// Bussit / linja-autot, M3: Yli 5000 kg
	// Istuinpaikkoja 19 - 85 (tilausajot/kuljetukset), seisomapaikkoineen 150 asti, myös pikkubusseja (joukko-/paikallisliikenne)
	// tilausajot, VPL/SPL kuljetuksia/tehtäviä, koulukuljetuksia, kelakuljetuksia, esi- ja peruskoululaisten erilliskuljetukset, eläkeläisten kuljetukset, muut tilaus/yksittäiskuljetukset (linja-autolla tehtävissä, istuinpaikkojen mukaan)

	@Column
	private String total_amount_of_passengers;

	@Column
	private String total_amount_of_double_doors;

	@Column
	// Matala/korkealattiamalli
	private String floor_type;

	@Column
	// Ovien sijainti (etu/keski/taka)
	private String location_of_doors;

	@Column
	// Vmax (km/h)
	private String vmax;

	@Column
	private Boolean is_double_decker_bus = false;

	@Column
	// Ylitys edessä (m)
	private String crossing_in_front;

	@Column
	// Ylitys takana (m)
	private String crossing_in_back;

	@Column
	// Seisomakorkeus edessä/takana (m)
	private String standing_height_front_and_back;



	// Kevyt kuorma-autot, kuorma-autot, N2: 3500 - 12000 kg
	// Palveluliikenne (rahti), tieliikenteen postikuljetukset, pakettien kuljetuspalvelut, postin jakelupalvelut, pakettien jakelupalvelut, huoltoajoneuvo, jätteiden keruupalvelut

	@Column
	private String total_amount_of_side_doors;

	@Column
	private Boolean is_whole_side_openable = false;

	@Column
	// Kuormatilatyyppi / päällirakenne (umpikuorma, avolava, nosturiauto, pressukuorma, jätteenkeräys, auraus/hiekoitus, koukkulaite, vaijerilaite, säiliö, muu)
	private String cargo_space_type;

	@Column
	// Pidennetty ohjaamo
	private Boolean is_extended_cab;

	@Column
	// Ohjaamon lattian korkeus maasta
	private String height_of_cab_floor_from_ground;

	@Column
	// Pantografi-virroitin
	private String pantograph;



	// Kuorma-autot, N3: Yli 12000 kg
	// Kaukoliikenne 18 - 80 tonnia
	// Jakeluliikenne 18 - 80 tonnia
	// Raskaskuljetukset 250 tonniin asti
	//
	// Vetoautot ja päällirakenneautot
	// Voi soveltaa myös irtotrailerille
	//
	// Palveluliikenne (rahti), tieliikenteen postikuljetukset, pakettien kuljetuspalvelut, postin jakelupalvelut, pakettien jakelupalvelut, huoltoajoneuvo, jätteiden keruupalvelut

	@Column
	private String amount_of_gears;

	@Column
	// Moottorijarrun teho (kW)
	private String power_of_motor_brake;

	@Column
	// Suurtehomoottorijarru (kW)
	private String high_power_engine_brake;

	@Column
	// Korkeajänniteakut määrä (kpl)
	private String amount_of_high_voltage_batteries;

	@Column
	// Taka-akselin kallistuksenvakain
	private String rear_axle_tilt_stabilizer;

	@Column
	// Taka-akselin tuenta
	private String rear_axle_support;

	@Column
	// Tasauspyörästön lukot vetävillä akseleilla
	private String differential_locks_on_drive_shaft;



	@Transient
	@JsonIgnore
	@ToString.Exclude
	private Boolean isSelected = false;
}
