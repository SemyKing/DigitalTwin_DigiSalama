package com.example.demo.api.rest_controllers.vehicle;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.vehicle.Fleet;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.repositories.EventHistoryLogRepository;
import com.example.demo.database.repositories.OrganisationRepository;
import com.example.demo.database.repositories.vehicle.FleetRepository;
import com.example.demo.database.repositories.vehicle.VehicleRepository;
import com.example.demo.utils.Constants;
import com.example.demo.utils.DateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(PER_CLASS)
class VehicleRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private VehicleRepository vehicleRepository;
	@MockBean
	private OrganisationRepository organisationRepository;
	@MockBean
	private FleetRepository fleetRepository;
	@MockBean
	private EventHistoryLogRepository eventHistoryLogRepository;



	private final String ENTITY = "vehicle";
	private final String URL = Constants.JSON_API + "/vehicles";

	private List<Vehicle> vehicleList;


	private final Long ID_NOT_FOUND = 0L;
	private final Long ID_FOUND = 1L;

	@BeforeAll
	public void init() {
		vehicleList = new ArrayList<>();
	}


	@Test
	public void testPostList_EmptyList() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders
				.post(URL + "/batch")
				.content(objectMapper.writeValueAsString(new ArrayList<>()))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
				.andExpect(status().reason(containsString("NULL or empty array was provided")))
				.andDo(print());
	}

	@Test
	public void testPostList_NullList() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders
				.post(URL + "/batch")
				.content(objectMapper.writeValueAsString(null))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
				.andDo(print());
	}

	@Test
	public void testPostList_NullElementAndNonNullElement() throws Exception {
		Vehicle toBeSaved = getVehicle(null, "vehicle_name");
		Mockito.when(vehicleRepository.save(toBeSaved)).thenReturn(toBeSaved);

		vehicleList.clear();
		vehicleList.add(null);
		vehicleList.add(toBeSaved);

		this.mockMvc.perform(MockMvcRequestBuilders
				.post(URL + "/batch")
				.content(objectMapper.writeValueAsString(vehicleList))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.MULTI_STATUS.value()))

				.andExpect(jsonPath("$[0].body").value(IsNull.nullValue()))
				.andExpect(jsonPath("$[0].http_status").value("BAD_REQUEST"))
				.andExpect(jsonPath("$[0].message").value("provided NULL entity"))

				.andExpect(jsonPath("$[1].http_status").value("OK"))
				.andExpect(jsonPath("$[1].message").value(ENTITY + " saved successfully"))

				.andDo(print());
	}

	@Test
	public void testPostList_InternalServerError() throws Exception {
		vehicleList.clear();
		vehicleList.add(getVehicle(null, "vehicle_name"));

		this.mockMvc.perform(MockMvcRequestBuilders
				.post(URL + "/batch")
				.content(objectMapper.writeValueAsString(vehicleList))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.MULTI_STATUS.value()))
				.andExpect(jsonPath("$[0].http_status").value("INTERNAL_SERVER_ERROR"))
				.andExpect(jsonPath("$[0].message").value("failed to save " + ENTITY + " in database"))
				.andDo(print());
	}



	@Test
	public void testPost_Null() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders
				.post(URL)
				.content(objectMapper.writeValueAsString(null))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
				.andDo(print());
	}

	@Test
	public void testPost_NonNullElementWithEmptyString() throws Exception {
		Vehicle toBeSaved = getVehicle(null, "vehicle_name");
		Mockito.when(vehicleRepository.save(toBeSaved)).thenReturn(toBeSaved);

		this.mockMvc.perform(MockMvcRequestBuilders
				.post(URL)
				.content(objectMapper.writeValueAsString(getVehicle(null, "")))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
				.andExpect(jsonPath("$.http_status").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").value("'name' cannot be empty"))
				.andDo(print())
				.andReturn();
	}

	@Test
	public void testPost_NonNullElement() throws Exception {
		Vehicle toBeSaved = getVehicle(null, "vehicle_name");
		Mockito.when(vehicleRepository.save(toBeSaved)).thenReturn(toBeSaved);

		this.mockMvc.perform(MockMvcRequestBuilders
				.post(URL)
				.content(objectMapper.writeValueAsString(toBeSaved))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.http_status").value("OK"))
				.andExpect(jsonPath("$.message").value(ENTITY + " saved successfully"))
				.andDo(print());
	}

	@Test
	public void testPost_InternalServerError() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders
				.post(URL)
				.content(objectMapper.writeValueAsString(getVehicle(null, "vehicle_name")))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()))
				.andExpect(jsonPath("$.http_status").value("INTERNAL_SERVER_ERROR"))
				.andExpect(jsonPath("$.message").value("failed to save " + ENTITY + " in database"))
				.andDo(print());
	}

	@Test
	public void testPost_ById() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders
				.post(URL + "/1")
				.content(objectMapper.writeValueAsString(getVehicle(null, "vehicle_name")))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
				.andExpect(status().reason(containsString("POST method with ID parameter not allowed")))
				.andDo(print());
	}



	@Test
	public void testGet_ByIdNotFound() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders
				.get(URL + "/" + ID_NOT_FOUND)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.NOT_FOUND.value()))
				.andExpect(status().reason(containsString(ENTITY + " with ID: '" + ID_NOT_FOUND + "' not found")))
				.andDo(print());
	}

	@Test
	public void testGet_ByIdFound() throws Exception {
		Optional<Vehicle> vehicle = Optional.of(getVehicle(ID_FOUND, "found"));
		Mockito.when(vehicleRepository.findById(ID_FOUND)).thenReturn(vehicle);

		this.mockMvc.perform(MockMvcRequestBuilders
				.get(URL + "/" + ID_FOUND)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.name").value("found"))
				.andDo(print());
	}



	@Test
	public void testPutList_EmptyList() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders
				.put(URL)
				.content(objectMapper.writeValueAsString(new ArrayList<>()))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
				.andExpect(status().reason(containsString("NULL or empty array was provided")))
				.andDo(print());
	}

	@Test
	public void testPutList_NullList() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders
				.put(URL)
				.content(objectMapper.writeValueAsString(null))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
				.andDo(print());
	}

	@Test
	public void testPutList_NullElementAndNonNullElement() throws Exception {
		Vehicle toBeSaved = getVehicle(ID_FOUND, "vehicle_name_put");
		Optional<Vehicle> vehicle = Optional.of(getVehicle(ID_FOUND, "found"));

		Mockito.when(vehicleRepository.findById(ID_FOUND)).thenReturn(vehicle);
		Mockito.when(vehicleRepository.save(toBeSaved)).thenReturn(toBeSaved);

		vehicleList.clear();
		vehicleList.add(null);
		vehicleList.add(toBeSaved);

		this.mockMvc.perform(MockMvcRequestBuilders
				.put(URL)
				.content(objectMapper.writeValueAsString(vehicleList))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.MULTI_STATUS.value()))

				.andExpect(jsonPath("$[0].body").value(IsNull.nullValue()))
				.andExpect(jsonPath("$[0].http_status").value("BAD_REQUEST"))
				.andExpect(jsonPath("$[0].message").value("provided NULL entity"))

				.andExpect(jsonPath("$[1].body.name").value("vehicle_name_put"))
				.andExpect(jsonPath("$[1].http_status").value("OK"))
				.andExpect(jsonPath("$[1].message").value(ENTITY + " saved successfully"))

				.andDo(print());
	}

	@Test
	public void testPutList_InternalServerError() throws Exception {
		Optional<Vehicle> vehicle = Optional.of(getVehicle(ID_FOUND, "found"));
		Mockito.when(vehicleRepository.findById(ID_FOUND)).thenReturn(vehicle);

		vehicleList.clear();
		vehicleList.add(getVehicle(ID_FOUND, "vehicle_name"));

		this.mockMvc.perform(MockMvcRequestBuilders
				.put(URL)
				.content(objectMapper.writeValueAsString(vehicleList))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.MULTI_STATUS.value()))
				.andExpect(jsonPath("$[0].http_status").value("INTERNAL_SERVER_ERROR"))
				.andExpect(jsonPath("$[0].message").value("failed to save " + ENTITY + " in database"))
				.andDo(print());
	}



	@Test
	public void testPut_Null() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders
				.put(URL + "/" + ID_FOUND)
				.content(objectMapper.writeValueAsString(null))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
				.andDo(print());
	}

	@Test
	public void testPut_NonNullElementWithoutId() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders
				.put(URL + "/" + ID_FOUND)
				.content(objectMapper.writeValueAsString(getVehicle(null, "vehicle_name")))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
				.andExpect(jsonPath("$.http_status").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").value("ID parameter is required"))
				.andDo(print())
				.andReturn();
	}

	@Test
	public void testPut_NonNullElementWithEmptyString() throws Exception {
		Optional<Vehicle> vehicle = Optional.of(getVehicle(ID_FOUND, "found"));
		Mockito.when(vehicleRepository.findById(ID_FOUND)).thenReturn(vehicle);

		this.mockMvc.perform(MockMvcRequestBuilders
				.put(URL + "/" + ID_FOUND)
				.content(objectMapper.writeValueAsString(getVehicle(ID_FOUND, "")))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
				.andExpect(jsonPath("$.http_status").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").value("'name' cannot be empty"))
				.andDo(print())
				.andReturn();
	}

	@Test
	public void testPut_NonNullElement() throws Exception {
		Vehicle toBeSaved = getVehicle(ID_FOUND, "vehicle_name_put");
		Optional<Vehicle> vehicle = Optional.of(getVehicle(ID_FOUND, "found"));

		Mockito.when(vehicleRepository.findById(ID_FOUND)).thenReturn(vehicle);
		Mockito.when(vehicleRepository.save(toBeSaved)).thenReturn(toBeSaved);

		this.mockMvc.perform(MockMvcRequestBuilders
				.put(URL + "/" + ID_FOUND)
				.content(objectMapper.writeValueAsString(toBeSaved))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.http_status").value("OK"))
				.andExpect(jsonPath("$.message").value(ENTITY + " saved successfully"))
				.andDo(print());
	}

	@Test
	public void testPut_InternalServerError() throws Exception {
		Optional<Vehicle> vehicle = Optional.of(getVehicle(ID_FOUND, "found"));
		Mockito.when(vehicleRepository.findById(ID_FOUND)).thenReturn(vehicle);

		this.mockMvc.perform(MockMvcRequestBuilders
				.put(URL + "/" + ID_FOUND)
				.content(objectMapper.writeValueAsString(getVehicle(ID_FOUND, "vehicle_name")))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()))
				.andExpect(jsonPath("$.http_status").value("INTERNAL_SERVER_ERROR"))
				.andExpect(jsonPath("$.message").value("failed to save " + ENTITY + " in database"))
				.andDo(print());
	}



	@Test
	public void testPatchList_EmptyList() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders
				.patch(URL)
				.content(objectMapper.writeValueAsString(new ArrayList<>()))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
				.andExpect(status().reason(containsString("NULL or empty array was provided")))
				.andDo(print());
	}

	@Test
	public void testPatchList_NullList() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders
				.patch(URL)
				.content(objectMapper.writeValueAsString(null))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
				.andDo(print());
	}

	@Test
	public void testPatchList_NullElement() throws Exception {
		vehicleList.clear();
		vehicleList.add(null);

		this.mockMvc.perform(MockMvcRequestBuilders
				.patch(URL)
				.content(objectMapper.writeValueAsString(vehicleList))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.MULTI_STATUS.value()))
				.andExpect(jsonPath("$[0].http_status").value("BAD_REQUEST"))
				.andExpect(jsonPath("$[0].message").value("NULL array element was provided"))
				.andDo(print())
				.andReturn();
	}

	@Test
	public void testPatchList_NullElementAndNonNullElement() throws Exception {
		Map<String, Object> changes = new HashMap<>();
		changes.put("id", ID_FOUND);
		changes.put("name", "vehicle_name_put");
		changes.put("required_driving_licence", "A");
		changes.put("commissioning_check_performed_date", "1855-02-25 11:25");

		Optional<Vehicle> vehicle = Optional.of(getVehicle(ID_FOUND, "found"));

		Vehicle toBeSaved = getVehicle(ID_FOUND, "vehicle_name_patch");
		toBeSaved.setRequired_driving_licence("A");
		toBeSaved.setCommissioning_check_performed_date(LocalDateTime.parse("1855-02-25 11:25"));

		Mockito.when(vehicleRepository.findById(ID_FOUND)).thenReturn(vehicle);
		Mockito.when(vehicleRepository.save(vehicle.get())).thenReturn(toBeSaved);

		List<Map<String, Object>> list = new ArrayList<>();
		list.add(null);
		list.add(changes);

		this.mockMvc.perform(MockMvcRequestBuilders
				.patch(URL)
				.content(objectMapper.writeValueAsString(list))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.MULTI_STATUS.value()))

				.andExpect(jsonPath("$[0].body").value(IsNull.nullValue()))
				.andExpect(jsonPath("$[0].http_status").value("BAD_REQUEST"))
				.andExpect(jsonPath("$[0].message").value("NULL array element was provided"))

				.andExpect(jsonPath("$[1].body.name").value("vehicle_name_patch"))
				.andExpect(jsonPath("$[1].http_status").value("OK"))
				.andExpect(jsonPath("$[1].message").value("vehicle patched successfully"))

				.andDo(print());
	}



	@Test
	public void testPatch_Null() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders
				.patch(URL + "/" + ID_FOUND)
				.content(objectMapper.writeValueAsString(null))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
				.andDo(print());
	}

	@Test
	public void testPatch_NonNullElementWithEmptyString() throws Exception {
		Vehicle toBeSaved = getVehicle(ID_FOUND, "");
		Optional<Vehicle> vehicle = Optional.of(getVehicle(ID_FOUND, "found"));

		Mockito.when(vehicleRepository.findById(ID_FOUND)).thenReturn(vehicle);
		Mockito.when(vehicleRepository.save(vehicle.get())).thenReturn(toBeSaved);

		this.mockMvc.perform(MockMvcRequestBuilders
				.patch(URL + "/" + ID_FOUND)
				.content(objectMapper.writeValueAsString(toBeSaved))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
				.andExpect(jsonPath("$.http_status").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").value("'name' cannot be empty"))
				.andDo(print())
				.andReturn();
	}

	@Test
	public void testPatch_NonNullElement() throws Exception {
		Map<String, Object> changes = new HashMap<>();
		changes.put("name", "vehicle_name_patch");
		changes.put("required_driving_licence", "A");
		changes.put("commissioning_check_performed_date", "1999-03-26 12:35");

		Vehicle toBeSaved = getVehicle(ID_FOUND, "vehicle_name_patch");
		toBeSaved.setRequired_driving_licence("A");
		toBeSaved.setCommissioning_check_performed_date(LocalDateTime.parse("1999-03-26 12:35"));

		Optional<Vehicle> vehicle = Optional.of(getVehicle(ID_FOUND, "found"));

		Mockito.when(vehicleRepository.findById(ID_FOUND)).thenReturn(vehicle);
		Mockito.when(vehicleRepository.save(vehicle.get())).thenReturn(toBeSaved);

		this.mockMvc.perform(MockMvcRequestBuilders
				.patch(URL + "/" + ID_FOUND)
				.content(objectMapper.writeValueAsString(changes))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.http_status").value("OK"))
				.andExpect(jsonPath("$.message").value(ENTITY + " saved successfully"))
				.andDo(print());
	}

	@Test
	public void testPatch_changeOrganisation() throws Exception {

		Organisation organisation = new Organisation();
		organisation.setId(11L);
		organisation.setName("VEDIA");

		Mockito.when(organisationRepository.findById(11L)).thenReturn(Optional.of(organisation));

		Map<String, Object> changes = new HashMap<>();
		changes.put("name", "vehicle_name_patch");
		changes.put("organisation", "{\"id\":11,\"name\":\"VEDIA\"}");

		Vehicle toBeSaved = getVehicle(ID_FOUND, "vehicle_name_patch");
		toBeSaved.setOrganisation(organisation);

		Optional<Vehicle> vehicle = Optional.of(getVehicle(ID_FOUND, "found"));

		Mockito.when(vehicleRepository.findById(ID_FOUND)).thenReturn(vehicle);
		Mockito.when(vehicleRepository.save(vehicle.get())).thenReturn(toBeSaved);



		this.mockMvc.perform(MockMvcRequestBuilders
				.patch(URL + "/" + ID_FOUND)
				.content(objectMapper.writeValueAsString(changes))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.http_status").value("OK"))
				.andExpect(jsonPath("$.message").value(ENTITY + " patched successfully"))
				.andDo(print());
	}

	@Test
	public void testPatch_changeFleets() throws Exception {

		Fleet fleet1 = getFleet(1L, "FLEET 1");
		Fleet fleet2 = getFleet(2L, "FLEET 2");

		Set<Fleet> fleets = new HashSet<>(Arrays.asList(fleet1, fleet2));

		Mockito.when(fleetRepository.findById(1L)).thenReturn(Optional.of(fleet1));
		Mockito.when(fleetRepository.findById(2L)).thenReturn(Optional.of(fleet2));

		Map<String, Object> changes = new HashMap<>();
		changes.put("name", "vehicle_name_patch");
		changes.put("fleets", objectMapper.writeValueAsString(fleets));

		Vehicle toBeSaved = getVehicle(ID_FOUND, "vehicle_name_patch");
		toBeSaved.setFleets(fleets);

		fleet1.getVehicles().add(toBeSaved);
		fleet2.getVehicles().add(toBeSaved);

		Optional<Vehicle> vehicle = Optional.of(getVehicle(ID_FOUND, "found"));

		Mockito.when(vehicleRepository.findById(ID_FOUND)).thenReturn(vehicle);
		Mockito.when(vehicleRepository.save(vehicle.get())).thenReturn(toBeSaved);



		this.mockMvc.perform(MockMvcRequestBuilders
				.patch(URL + "/" + ID_FOUND)
				.content(objectMapper.writeValueAsString(changes))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.http_status").value("OK"))
				.andExpect(jsonPath("$.message").value(ENTITY + " patched successfully"))
				.andDo(print());
	}

	@Test
	public void testPatch_InternalServerError() throws Exception {
		Optional<Vehicle> vehicle = Optional.of(getVehicle(ID_FOUND, "found"));
		Mockito.when(vehicleRepository.findById(ID_FOUND)).thenReturn(vehicle);

		this.mockMvc.perform(MockMvcRequestBuilders
				.patch(URL + "/" + ID_FOUND)
				.content(objectMapper.writeValueAsString(getVehicle(ID_FOUND, "vehicle_name")))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()))
				.andExpect(jsonPath("$.http_status").value("INTERNAL_SERVER_ERROR"))
				.andExpect(jsonPath("$.message").value("failed to save " + ENTITY + " in database"))
				.andDo(print());
	}



	@Test
	public void testDeleteList_EmptyList() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders
				.delete(URL)
				.content(objectMapper.writeValueAsString(new ArrayList<>()))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
				.andExpect(status().reason(containsString("NULL or empty array was provided")))
				.andDo(print());
	}

	@Test
	public void testDeleteList_NullList() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders
				.delete(URL)
				.content(objectMapper.writeValueAsString(null))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
				.andDo(print());
	}

	@Test
	public void testDeleteList_NullElementAndNonNullElement() throws Exception {
		Optional<Vehicle> vehicle = Optional.of(getVehicle(ID_FOUND, "found"));
		Vehicle toBeSaved = getVehicle(ID_FOUND, "vehicle_name_delete");

		Mockito.when(vehicleRepository.findById(ID_FOUND)).thenReturn(vehicle);

		vehicleList.clear();
		vehicleList.add(null);
		vehicleList.add(toBeSaved);

		this.mockMvc.perform(MockMvcRequestBuilders
				.delete(URL)
				.content(objectMapper.writeValueAsString(vehicleList))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.MULTI_STATUS.value()))

				.andExpect(jsonPath("$[0].body").value(IsNull.nullValue()))
				.andExpect(jsonPath("$[0].http_status").value("BAD_REQUEST"))
				.andExpect(jsonPath("$[0].message").value("provided NULL entity"))

				.andExpect(jsonPath("$[1].body.name").value("vehicle_name_delete"))
				.andExpect(jsonPath("$[1].http_status").value("OK"))
				.andExpect(jsonPath("$[1].message").value("vehicle deleted successfully"))

				.andDo(print());
	}

	@Test
	public void testDeleteList_NonNullElementWithoutId() throws Exception {
		Optional<Vehicle> vehicle = Optional.of(getVehicle(ID_FOUND, "found"));
		Vehicle toBeSaved = getVehicle(null, "vehicle_name_delete");

		Mockito.when(vehicleRepository.findById(ID_FOUND)).thenReturn(vehicle);

		vehicleList.clear();
		vehicleList.add(toBeSaved);

		this.mockMvc.perform(MockMvcRequestBuilders
				.delete(URL)
				.content(objectMapper.writeValueAsString(vehicleList))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.MULTI_STATUS.value()))

				.andExpect(jsonPath("$[0].http_status").value("BAD_REQUEST"))
				.andExpect(jsonPath("$[0].message").value("ID parameter is required"))

				.andDo(print());
	}



	@Test
	public void testDelete_IdNotFound() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders
				.delete(URL + "/" + ID_NOT_FOUND)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.NOT_FOUND.value()))
				.andExpect(status().reason(containsString(ENTITY + " with ID: '" + ID_NOT_FOUND + "' not found")))
				.andDo(print())
				.andReturn();
	}

	@Test
	public void testDelete_NonNullElement() throws Exception {
		Optional<Vehicle> vehicle = Optional.of(getVehicle(ID_FOUND, "found"));
		Mockito.when(vehicleRepository.findById(ID_FOUND)).thenReturn(vehicle);

		this.mockMvc.perform(MockMvcRequestBuilders
				.delete(URL + "/" + ID_FOUND)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.http_status").value("OK"))
				.andExpect(jsonPath("$.message").value(ENTITY + " deleted successfully"))
				.andDo(print())
				.andReturn();
	}




	private Vehicle getVehicle(Long id, String name) {
		Vehicle vehicle = new Vehicle();
		vehicle.setId(id);
		vehicle.setName(name);
		return vehicle;
	}

	private Fleet getFleet(Long id, String name) {
		Fleet fleet = new Fleet();
		fleet.setId(id);
		fleet.setName(name);
		return fleet;
	}
}