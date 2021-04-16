package com.example.demo.api.controllers;

import com.example.demo.database.models.user.User;
import com.example.demo.database.models.utils.JwtResponse;
import com.example.demo.database.models.utils.ListWrapper;
import com.example.demo.database.repositories.RoleRepository;
import com.example.demo.database.repositories.UserRepository;
import com.example.demo.database.services.UserService;
import com.example.demo.utils.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    @MockBean
    private UserRepository userRepository;


    private final String AUTHENTICATE =  StringUtils.JSON_API + "/authenticate";
    private final String USERS = StringUtils.JSON_API + "/users";
    private final String USERS_LIST = StringUtils.JSON_API + "/users/batch";

    private String TOKEN = "";
    private String NON_EXISTENT_ID = "99999999";

    @BeforeAll
    public void init() {

    }

    @Test
    public void testGetAllUsersBeforeAuthentication() throws Exception {
        this.mockMvc.perform(get(USERS))
                .andExpect(status().is(401))
                .andExpect(status().reason(containsString("Full authentication is required to access this resource")))
                .andDo(print());
    }

    @Test
    public void testAuthenticateInvalidUser() throws Exception {
        User userInvalid1 = new User();
        userInvalid1.setUsername("test_username_invalid1");
        userInvalid1.setPassword(userService.getBcryptEncoder().encode("password"));

        User userInvalid2 = new User();
        userInvalid2.setUsername("test_username_invalid1");
        userInvalid2.setPassword("password");

        doReturn(Optional.of(userInvalid1)).when(userRepository).findUserByUsername("test_username_invalid1");


        this.mockMvc.perform(MockMvcRequestBuilders
                .post(AUTHENTICATE)
                .content(objectMapper.writeValueAsString(userInvalid2))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(content().string(containsString("Invalid credentials")))
                .andDo(print());
    }

    @Test
    public void testAuthenticateNonExistentUser() throws Exception {
        User userNonExistent = new User();
        userNonExistent.setUsername("test_username_non_existent");
        userNonExistent.setPassword("password");

        this.mockMvc.perform(MockMvcRequestBuilders
                .post(AUTHENTICATE)
                .content(objectMapper.writeValueAsString(userNonExistent))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(content().string("Invalid credentials"))
                .andDo(print());
    }

    @Test
    public void testAuthenticateSuccess() throws Exception {

        User userValid = new User();
        userValid.setUsername("test_username_valid");
        userValid.setPassword(userService.getBcryptEncoder().encode("password"));
        userValid.setRole(roleRepository.findByName(StringUtils.ROLE_USER));


        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post(AUTHENTICATE)
                .content(objectMapper.writeValueAsString(userValid))
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
    public void testGetAllUsersAfterAuthentication() throws Exception {
        this.mockMvc.perform(get(USERS).header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().is(200))
                .andDo(print());
    }



    // POST

    @Test
    public void testPostUsersListFail() throws Exception {
        User testUser = new User();
        testUser.setUsername(getNewUsername());
        testUser.setPassword("password");

        List<User> users = new ArrayList<>();
        users.add(null);
        users.add(testUser);

        this.mockMvc.perform(MockMvcRequestBuilders
                .post(USERS_LIST).header("Authorization", "Bearer " + TOKEN)
                .content(objectMapper.writeValueAsString(users))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(207))
                .andDo(print());
    }
    @Test
    public void testPostUsersListSuccess() throws Exception {
        User testUser = new User();
        testUser.setUsername(getNewUsername());
        testUser.setPassword("password");

        List<User> users = new ArrayList<>();
        users.add(testUser);

        this.mockMvc.perform(MockMvcRequestBuilders
                .post(USERS_LIST).header("Authorization", "Bearer " + TOKEN)
                .content(objectMapper.writeValueAsString(users))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andDo(print());
    }

    @Test
    public void testPostUserFail() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                .post(USERS).header("Authorization", "Bearer " + TOKEN)
                .content(objectMapper.writeValueAsString(new User()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andDo(print());
    }

    @Test
    public void testPostUserSuccess() throws Exception {
        User testUser = new User();
        testUser.setUsername(getNewUsername());
        testUser.setPassword("password");

        this.mockMvc.perform(MockMvcRequestBuilders
                .post(USERS).header("Authorization", "Bearer " + TOKEN)
                .content(objectMapper.writeValueAsString(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andDo(print());
    }


    // PUT

    @Test
    public void testPutUsersListFail() throws Exception {
        User testUser = new User();
        testUser.setUsername(getNewUsername());
        testUser.setPassword("password");

        List<User> users = new ArrayList<>();
        users.add(null);
        users.add(testUser);

        this.mockMvc.perform(MockMvcRequestBuilders
                .put(USERS).header("Authorization", "Bearer " + TOKEN)
                .content(objectMapper.writeValueAsString(users))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(207))
                .andDo(print());
    }


    private String putSuccessUsername = "";

    @Test
    public void testPutUsersListSuccess() throws Exception {

        putSuccessUsername = getNewUsername();

        User testUser = new User();
        testUser.setUsername(putSuccessUsername);
        testUser.setPassword("password");

        List<User> users = new ArrayList<>();
        users.add(testUser);

        this.mockMvc.perform(MockMvcRequestBuilders
                .put(USERS).header("Authorization", "Bearer " + TOKEN)
                .content(objectMapper.writeValueAsString(users))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andDo(print());
    }

    @Test
    public void testPutUserFail() throws Exception {
        User testUser = userService.getByUsername(putSuccessUsername);
        testUser.setFirst_name("FIRST_NAME_PUT_TEST_FAIL");

        this.mockMvc.perform(MockMvcRequestBuilders
                .put(USERS + "/" + NON_EXISTENT_ID).header("Authorization", "Bearer " + TOKEN)
                .content(objectMapper.writeValueAsString(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(content().string(containsString("ID parameter is invalid")))
                .andDo(print());
    }

    @Test
    public void testPutUserSuccess() throws Exception {
        User testUser = userService.getByUsername(putSuccessUsername);
        testUser.setFirst_name("FIRST_NAME_PUT_TEST_SUCCESS");

        this.mockMvc.perform(MockMvcRequestBuilders
                .put(USERS + "/" + testUser.getId()).header("Authorization", "Bearer " + TOKEN)
                .content(objectMapper.writeValueAsString(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(content().string(containsString("FIRST_NAME_PUT_TEST_SUCCESS")))
                .andDo(print());
    }

    @Test
    public void testPatchUserFail() throws Exception {
        User testUser = userService.getByUsername(putSuccessUsername);
        testUser.setLast_name("LAST_NAME_PATCH_TEST_FAIL");

        this.mockMvc.perform(MockMvcRequestBuilders
                .patch(USERS + "/" + NON_EXISTENT_ID).header("Authorization", "Bearer " + TOKEN)
                .content(objectMapper.writeValueAsString(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andDo(print());
    }

    @Test
    public void testPatchUserSuccess() throws Exception {
        User testUser = userService.getByUsername(putSuccessUsername);
        testUser.setLast_name("LAST_NAME_PATCH_TEST_SUCCESS");

        this.mockMvc.perform(MockMvcRequestBuilders
                .patch(USERS + "/" + testUser.getId()).header("Authorization", "Bearer " + TOKEN)
                .content(objectMapper.writeValueAsString(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(content().string(containsString("LAST_NAME_PATCH_TEST_SUCCESS")))
                .andDo(print());
    }

    @AfterAll
    public void clean() {
//        userService.delete(userValid);
//        userService.delete(userInvalid);
//        userService.delete(userNonExistent);
    }



    private static int number = 0;

    private String getNewUsername() {
        number++;
        return "test_username_" + number;
    }
}