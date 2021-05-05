package com.example.demo.api.ui_controllers;

import com.example.demo.database.models.user.User;
import com.example.demo.database.models.utils.JwtResponse;
import com.example.demo.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(PER_CLASS)
class GeneralControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;



    private final String URL = Constants.JSON_API + "/vehicles";
    private final String AUTHENTICATE = Constants.JSON_API + "/authenticate";


    @Value("${tests.username}")
    private String REAL_USER;

    @Value("${tests.password}")
    private String REAL_PASSWORD;

    private String JWT_TOKEN = "";


    @Test
    @BeforeAll
    public void test_authenticate_valid_user() throws Exception {
        User validUser = new User();
        validUser.setUsername(REAL_USER);
        validUser.setPassword(REAL_PASSWORD);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post(AUTHENTICATE)
                .content(objectMapper.writeValueAsString(validUser))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.jwt_token").exists())
                .andDo(print())
                .andReturn();

        JwtResponse jwtResponse = objectMapper.readValue(result.getResponse().getContentAsString(), JwtResponse.class);
        JWT_TOKEN = jwtResponse.getJwt_token();
    }

    @Test
    public void test_GET_before_authentication() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                .get(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(401))
                .andExpect(status().reason(containsString("Full authentication is required")))
                .andDo(print())
                .andReturn();
    }

    @Test
    public void test_authenticate_invalid_user() throws Exception {
        User invalidUser = new User();
        invalidUser.setUsername("USERNAME_______________");
        invalidUser.setPassword("PASSWORD_______________");

        this.mockMvc.perform(MockMvcRequestBuilders
                .post(AUTHENTICATE)
                .content(objectMapper.writeValueAsString(invalidUser))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(content().string(containsString("Invalid credentials")))
                .andDo(print())
                .andReturn();
    }

    @Test
    public void test_GET_after_authentication() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                .get(URL).header("Authorization", "Bearer " + JWT_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andDo(print())
                .andReturn();
    }

}