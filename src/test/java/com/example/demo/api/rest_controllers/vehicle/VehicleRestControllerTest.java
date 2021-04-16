package com.example.demo.api.rest_controllers.vehicle;

import com.example.demo.database.models.user.User;
import com.example.demo.database.models.utils.JwtResponse;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.services.UserService;
import com.example.demo.database.services.vehicle.VehicleService;
import com.example.demo.utils.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.aspectj.lang.annotation.Before;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.servlet.ServletContext;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(PER_CLASS)
class VehicleRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private VehicleService vehicleService;

	@MockBean
	private UserService userService;


	private final String AUTHENTICATE = StringUtils.JSON_API + "/authenticate";
	private final String GET_ALL = 		StringUtils.JSON_API + "/vehicles";

	private String TOKEN = "";

	private User user;


	@BeforeAll
	public void init() {
		user = new User();
		user.setUsername("test_username");
		user.setPassword("password");

		userService.save(user);
	}

	@Test
	public void testGetAllBeforeAuthentication() throws Exception {
		this.mockMvc.perform(get(GET_ALL))
				.andExpect(status().is(401))
				.andExpect(status().reason(containsString("Full authentication is required to access this resource")))
				.andDo(print());
	}

	@Test
	public void testAuthenticateSuccess() throws Exception {
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders
				.post(AUTHENTICATE)
				.content(objectMapper.writeValueAsString(user))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(200))
				.andExpect(jsonPath("$.jwt_token").exists())
				.andDo(print())
				.andReturn();

		JwtResponse jwtResponse = objectMapper.readValue(result.getResponse().getContentAsString(), JwtResponse.class);
		TOKEN = jwtResponse.getJwt_token();
	}

	@Test
	public void testGetAllAfterAuthentication() throws Exception {
		this.mockMvc.perform(get(GET_ALL).header("Authorization", "Bearer " + TOKEN))
				.andExpect(status().is(200))
				.andDo(print());
	}
}