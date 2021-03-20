package com.example.demo.api.rest_controllers;

import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.services.vehicle.VehicleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VehicleRestControllerTest {


	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private VehicleService service;

	private final String url = "http://localhost/api2/vehicles/";



	@Test
	public void deleteAll() {
		service.deleteAll();
		Assertions.assertThat(service.getAll().size() == 0);
	}

	@Test
	public void testGetAll() throws Exception {
		mockMvc.perform(get(url))
				.andExpect(status().isOk())
				.andReturn();
	}

	@Test
	public void testPutOne() throws Exception {
		Vehicle vehicle = new Vehicle();
		vehicle.setName("NEWLY ADDED");

		MvcResult mvcResult = mockMvc.perform(post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(vehicle))
		).andReturn();

		int status = mvcResult.getResponse().getStatus();
		assertEquals(200, status);

		String content = mvcResult.getResponse().getContentAsString();
		System.out.println("CONTENT: " + content);


//		mockMvc.perform(post(url))
//				.param()
//				.andExpect(status().isOk())
//				.andReturn();
	}



}