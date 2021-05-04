package com.example.demo.api.rest_controllers;

import com.example.demo.database.models.Organisation;
import com.example.demo.database.models.user.User;
import com.example.demo.database.models.utils.JwtResponse;
import com.example.demo.database.repositories.EventHistoryLogRepository;
import com.example.demo.database.repositories.OrganisationRepository;
import com.example.demo.database.repositories.UserRepository;
import com.example.demo.database.repositories.vehicle.FleetRepository;
import com.example.demo.database.repositories.vehicle.VehicleRepository;
import com.example.demo.database.services.UserService;
import com.example.demo.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(PER_CLASS)
class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;


    @MockBean
    private VehicleRepository vehicleRepository;
    @MockBean
    private OrganisationRepository organisationRepository;
    @MockBean
    private FleetRepository fleetRepository;
    @MockBean
    private EventHistoryLogRepository eventHistoryLogRepository;


    private final String ENTITY = "user";
    private final String URL = Constants.JSON_API + "/users";
    private final String AUTHENTICATE =  Constants.JSON_API + "/authenticate";


    @Value("${tests.username}")
    private String REAL_USER;

    @Value("${tests.password}")
    private String REAL_PASSWORD;


    private String JWT_TOKEN = "";

    private final Long ID_NOT_FOUND = 0L;
    private final Long ID_FOUND = 1L;

    private List<User> userList;


//    when() requires an argument which has to be 'a method call on a mock'.

    @BeforeAll
    public void init() throws Exception {
        userList = new ArrayList<>();
    }

    @Test
    @BeforeEach
    public void getJWT_Token() throws Exception {
        if (JWT_TOKEN.length() <= 0) {

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
    }


    @Test
    public void testPostList_EmptyList() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                .post(URL + "/batch").header("Authorization", "Bearer " + JWT_TOKEN)
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
                .post(URL + "/batch").header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(null))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andDo(print());
    }

    @Test
    public void testPostList_NullElementAndNonNullElement() throws Exception {
        User toBeSaved = getUser(null, "user_name");
        Mockito.when(userService.save(toBeSaved)).thenReturn(toBeSaved);

        userList.clear();
        userList.add(null);
        userList.add(toBeSaved);

        this.mockMvc.perform(MockMvcRequestBuilders
                .post(URL + "/batch").header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(userList))
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
        userList.clear();
        userList.add(getUser(null, "user_name"));

        this.mockMvc.perform(MockMvcRequestBuilders
                .post(URL + "/batch").header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(userList))
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
                .post(URL).header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(null))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andDo(print());
    }

    @Test
    public void testPost_NonNullElementWithEmptyString() throws Exception {
        User toBeSaved = getUser(null, "user_name");
        Mockito.when(userService.save(toBeSaved)).thenReturn(toBeSaved);

        this.mockMvc.perform(MockMvcRequestBuilders
                .post(URL).header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(getUser(null, "")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.http_status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("cannot be empty")))
                .andDo(print())
                .andReturn();
    }

    @Test
    public void testPost_NonNullElement() throws Exception {
        User toBeSaved = getUser(null, "user_name");
        Mockito.when(userService.save(toBeSaved)).thenReturn(toBeSaved);

        this.mockMvc.perform(MockMvcRequestBuilders
                .post(URL).header("Authorization", "Bearer " + JWT_TOKEN)
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
                .post(URL).header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(getUser(null, "user_name")))
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
                .post(URL + "/1").header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(getUser(null, "user_name")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andExpect(status().reason(containsString("POST method with ID parameter not allowed")))
                .andDo(print());
    }



    @Test
    public void testGet_ByIdNotFound() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                .get(URL + "/" + ID_NOT_FOUND).header("Authorization", "Bearer " + JWT_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(status().reason(containsString(ENTITY + " with ID: '" + ID_NOT_FOUND + "' not found")))
                .andDo(print());
    }

    @Test
    public void testGet_ByIdFound() throws Exception {
        Optional<User> user = Optional.of(getUser(ID_FOUND, "found"));

//        User user = getUser(ID_FOUND, "found");
        Mockito.when(userService.getById(ID_FOUND)).thenReturn(user.get());

        this.mockMvc.perform(MockMvcRequestBuilders
                .get(URL + "/" + ID_FOUND).header("Authorization", "Bearer " + JWT_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("found"))
                .andDo(print());
    }



    @Test
    public void testPutList_EmptyList() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                .put(URL).header("Authorization", "Bearer " + JWT_TOKEN)
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
                .put(URL).header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(null))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andDo(print());
    }

    @Test
    public void testPutList_NullElementAndNonNullElement() throws Exception {
        User toBeSaved = getUser(ID_FOUND, "user_name_put");
        Optional<User> user = Optional.of(getUser(ID_FOUND, "found"));
//        User user = getUser(ID_FOUND, "found");

        Mockito.when(userService.getById(ID_FOUND)).thenReturn(user.get());
        Mockito.when(userService.save(toBeSaved)).thenReturn(toBeSaved);

        userList.clear();
        userList.add(null);
        userList.add(toBeSaved);

        this.mockMvc.perform(MockMvcRequestBuilders
                .put(URL).header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(userList))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.MULTI_STATUS.value()))

                .andExpect(jsonPath("$[0].body").value(IsNull.nullValue()))
                .andExpect(jsonPath("$[0].http_status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$[0].message").value("provided NULL entity"))

                .andExpect(jsonPath("$[1].body.name").value("user_name_put"))
                .andExpect(jsonPath("$[1].http_status").value("OK"))
                .andExpect(jsonPath("$[1].message").value(ENTITY + " saved successfully"))

                .andDo(print());
    }

    @Test
    public void testPutList_InternalServerError() throws Exception {
        Optional<User> user = Optional.of(getUser(ID_FOUND, "found"));
//        User user = getUser(ID_FOUND, "found");
        Mockito.when(userService.getById(ID_FOUND)).thenReturn(user.get());

        userList.clear();
        userList.add(getUser(ID_FOUND, "user_name"));

        this.mockMvc.perform(MockMvcRequestBuilders
                .put(URL).header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(userList))
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
                .put(URL + "/" + ID_FOUND).header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(null))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andDo(print());
    }

    @Test
    public void testPut_NonNullElementWithoutId() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                .put(URL + "/" + ID_FOUND).header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(getUser(null, "user_name")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.http_status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("entity ID parameter is required"))
                .andDo(print())
                .andReturn();
    }

    @Test
    public void testPut_NonNullElementWithEmptyString() throws Exception {
        Optional<User> user = Optional.of(getUser(ID_FOUND, "found"));
//        User user = getUser(ID_FOUND, "found");
        Mockito.when(userService.getById(ID_FOUND)).thenReturn(user.get());

        this.mockMvc.perform(MockMvcRequestBuilders
                .put(URL + "/" + ID_FOUND).header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(getUser(ID_FOUND, "")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.http_status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("cannot be empty")))
                .andDo(print())
                .andReturn();
    }

    @Test
    public void testPut_NonNullElement() throws Exception {
        User toBeSaved = getUser(ID_FOUND, "user_name_put");
        Optional<User> user = Optional.of(getUser(ID_FOUND, "found"));

        Mockito.when(userService.getById(ID_FOUND)).thenReturn(user.get());
        Mockito.when(userService.save(toBeSaved)).thenReturn(toBeSaved);

        this.mockMvc.perform(MockMvcRequestBuilders
                .put(URL + "/" + ID_FOUND).header("Authorization", "Bearer " + JWT_TOKEN)
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
        Optional<User> user = Optional.of(getUser(ID_FOUND, "found"));
//        User user = getUser(ID_FOUND, "found");
        Mockito.when(userService.getById(ID_FOUND)).thenReturn(user.get());

        this.mockMvc.perform(MockMvcRequestBuilders
                .put(URL + "/" + ID_FOUND).header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(getUser(ID_FOUND, "user_name")))
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
                .patch(URL).header("Authorization", "Bearer " + JWT_TOKEN)
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
                .patch(URL).header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(null))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andDo(print());
    }

    @Test
    public void testPatchList_NullElement() throws Exception {
        userList.clear();
        userList.add(null);

        this.mockMvc.perform(MockMvcRequestBuilders
                .patch(URL).header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(userList))
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
        changes.put("first_name", "first_name_patch");
        changes.put("last_name", "last_name_patch");

        Optional<User> user = Optional.of(getUser(ID_FOUND, "found"));

        User toBeSaved = getUser(ID_FOUND, "user_name_patch");
        toBeSaved.setFirst_name("first_name_put");
        toBeSaved.setLast_name("last_name_put");

        Mockito.when(userService.getById(ID_FOUND)).thenReturn(user.get());
        Mockito.when(userService.save(user.get())).thenReturn(toBeSaved);

        List<Map<String, Object>> list = new ArrayList<>();
        list.add(null);
        list.add(changes);

        this.mockMvc.perform(MockMvcRequestBuilders
                .patch(URL).header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(list))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.MULTI_STATUS.value()))

                .andExpect(jsonPath("$[0].body").value(IsNull.nullValue()))
                .andExpect(jsonPath("$[0].http_status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$[0].message").value("NULL array element was provided"))

                .andExpect(jsonPath("$[1].body.name").value("user_name_patch"))
                .andExpect(jsonPath("$[1].http_status").value("OK"))
                .andExpect(jsonPath("$[1].message").value("user patched successfully"))

                .andDo(print());
    }



    @Test
    public void testPatch_Null() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                .patch(URL + "/" + ID_FOUND).header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(null))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andDo(print());
    }

    @Test
    public void testPatch_NonNullElementWithEmptyString() throws Exception {
        User toBeSaved = getUser(ID_FOUND, "");
        Optional<User> user = Optional.of(getUser(ID_FOUND, "found"));

        Mockito.when(userService.getById(ID_FOUND)).thenReturn(user.get());
        Mockito.when(userService.save(user.get())).thenReturn(toBeSaved);

        this.mockMvc.perform(MockMvcRequestBuilders
                .patch(URL + "/" + ID_FOUND).header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(toBeSaved))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.http_status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("cannot be empty")))
                .andDo(print())
                .andReturn();
    }

    @Test
    public void testPatch_NonNullElement() throws Exception {
        Map<String, Object> changes = new HashMap<>();
        changes.put("name", "user_name_patch");
        changes.put("first_name", "first_name_put");
        changes.put("last_name", "last_name_put");

        User toBeSaved = getUser(ID_FOUND, "user_name_patch");
        toBeSaved.setFirst_name("first_name_put");
        toBeSaved.setLast_name("last_name_put");

        Optional<User> user = Optional.of(getUser(ID_FOUND, "found"));

        Mockito.when(userService.getById(ID_FOUND)).thenReturn(user.get());
        Mockito.when(userService.save(user.get())).thenReturn(toBeSaved);


        this.mockMvc.perform(MockMvcRequestBuilders
                .patch(URL + "/" + ID_FOUND).header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(changes))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.http_status").value("OK"))
                .andExpect(jsonPath("$.message").value(ENTITY + " patched successfully"))
                .andDo(print());
    }

    @Test
    public void testPatch_changeOrganisation() throws Exception {
        Organisation organisation = new Organisation();
        organisation.setId(11L);
        organisation.setName("VEDIA");

        Mockito.when(organisationRepository.findById(11L)).thenReturn(Optional.of(organisation));

        Map<String, Object> changes = new HashMap<>();
        changes.put("name", "user_name_patch");
//		changes.put("organisation", "{\"id\":11,\"name\":\"VEDIA\"}");
        changes.put("organisation", objectMapper.writeValueAsString(organisation));

        User toBeSaved = getUser(ID_FOUND, "user_name_patch");
        toBeSaved.setOrganisation(organisation);

        Optional<User> user = Optional.of(getUser(ID_FOUND, "found"));

        Mockito.when(userService.getById(ID_FOUND)).thenReturn(user.get());
        Mockito.when(userService.save(user.get())).thenReturn(toBeSaved);


        this.mockMvc.perform(MockMvcRequestBuilders
                .patch(URL + "/" + ID_FOUND).header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(changes))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.http_status").value("OK"))
                .andExpect(jsonPath("$.message").value(ENTITY + " patched successfully"))
                .andDo(print());
    }

//    @Test
//    public void testPatch_changeFleets() throws Exception {
//        Fleet fleet1 = getFleet(1L, "FLEET 1");
//        Fleet fleet2 = getFleet(2L, "FLEET 2");
//
//        Set<Fleet> fleets = new HashSet<>(Arrays.asList(fleet1, fleet2));
//
//        Mockito.when(fleetRepository.getById(1L)).thenReturn(Optional.of(fleet1));
//        Mockito.when(fleetRepository.getById(2L)).thenReturn(Optional.of(fleet2));
//
//        Map<String, Object> changes = new HashMap<>();
//        changes.put("name", "user_name_patch");
//        changes.put("fleets", objectMapper.writeValueAsString(fleets));
//
//        User toBeSaved = getUser(ID_FOUND, "user_name_patch");
//        toBeSaved.setFleets(fleets);
//
//        fleet1.getUsers().add(toBeSaved);
//        fleet2.getUsers().add(toBeSaved);
//
//        Optional<User> user = Optional.of(getUser(ID_FOUND, "found"));
//
//        Mockito.when(userService.getById(ID_FOUND)).thenReturn(user);
//        Mockito.when(userService.save(user.get())).thenReturn(toBeSaved);
//
//
//        this.mockMvc.perform(MockMvcRequestBuilders
//                .patch(URL + "/" + ID_FOUND).header("Authorization", "Bearer " + JWT_TOKEN)
//                .content(objectMapper.writeValueAsString(changes))
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().is(HttpStatus.OK.value()))
//                .andExpect(jsonPath("$.http_status").value("OK"))
//                .andExpect(jsonPath("$.message").value(ENTITY + " patched successfully"))
//                .andDo(print());
//    }

    @Test
    public void testPatch_InternalServerError() throws Exception {
        Optional<User> user = Optional.of(getUser(ID_FOUND, "found"));
        Mockito.when(userService.getById(ID_FOUND)).thenReturn(user.get());

        this.mockMvc.perform(MockMvcRequestBuilders
                .patch(URL + "/" + ID_FOUND).header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(getUser(ID_FOUND, "user_name")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(jsonPath("$.http_status").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("failed to save " + ENTITY + " in database"))
                .andDo(print());
    }

//    @Test
//    public void testPatch_InvalidDate() throws Exception {
//        Map<String, Object> changes = new HashMap<>();
//        changes.put("name", "user_name_patch");
//        changes.put("commissioning_check_performed_date", "1999-03-26T12");
//
//        User toBeSaved = getUser(ID_FOUND, "user_name_patch");
//        toBeSaved.setCommissioning_check_performed_date(LocalDateTime.parse("1999-03-26T12:35", DateUtils.getDatabaseFormat()));
//
//        Optional<User> user = Optional.of(getUser(ID_FOUND, "found"));
//
//        Mockito.when(vehicleRepository.getById(ID_FOUND)).thenReturn(vehicle);
//        Mockito.when(vehicleRepository.save(vehicle.get())).thenReturn(toBeSaved);
//
//        this.mockMvc.perform(MockMvcRequestBuilders
//                .patch(URL + "/" + ID_FOUND).header("Authorization", "Bearer " + JWT_TOKEN)
//                .content(objectMapper.writeValueAsString(changes))
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
//                .andExpect(jsonPath("$.http_status").value("BAD_REQUEST"))
//                .andExpect(jsonPath("$.message").value(containsString("Date: '1999-03-26T12' is invalid")))
//                .andDo(print());
//    }



    @Test
    public void testDeleteList_EmptyList() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                .delete(URL).header("Authorization", "Bearer " + JWT_TOKEN)
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
                .delete(URL).header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(null))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andDo(print());
    }

    @Test
    public void testDeleteList_NullElementAndNonNullElement() throws Exception {
        Optional<User> user = Optional.of(getUser(ID_FOUND, "found"));
        User toBeSaved = getUser(ID_FOUND, "vehicle_name_delete");

        Mockito.when(userService.getById(ID_FOUND)).thenReturn(user.get());

        userList.clear();
        userList.add(null);
        userList.add(toBeSaved);

        this.mockMvc.perform(MockMvcRequestBuilders
                .delete(URL).header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(userList))
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
        Optional<User> user = Optional.of(getUser(ID_FOUND, "found"));
        User toBeSaved = getUser(null, "vehicle_name_delete");

        Mockito.when(userService.getById(ID_FOUND)).thenReturn(user.get());

        userList.clear();
        userList.add(toBeSaved);

        this.mockMvc.perform(MockMvcRequestBuilders
                .delete(URL).header("Authorization", "Bearer " + JWT_TOKEN)
                .content(objectMapper.writeValueAsString(userList))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.MULTI_STATUS.value()))

                .andExpect(jsonPath("$[0].http_status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$[0].message").value("entity ID parameter is required"))

                .andDo(print());
    }



    @Test
    public void testDelete_IdNotFound() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                .delete(URL + "/" + ID_NOT_FOUND).header("Authorization", "Bearer " + JWT_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(status().reason(containsString(ENTITY + " with ID: '" + ID_NOT_FOUND + "' not found")))
                .andDo(print())
                .andReturn();
    }

    @Test
    public void testDelete_NonNullElement() throws Exception {
        Optional<User> user = Optional.of(getUser(ID_FOUND, "found"));
        Mockito.when(userService.getById(ID_FOUND)).thenReturn(user.get());

        this.mockMvc.perform(MockMvcRequestBuilders
                .delete(URL + "/" + ID_FOUND).header("Authorization", "Bearer " + JWT_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.http_status").value("OK"))
                .andExpect(jsonPath("$.message").value(ENTITY + " deleted successfully"))
                .andDo(print())
                .andReturn();
    }




    private User getUser(Long id, String firstName) {
        User user = new User();
        user.setId(id);
        user.setFirst_name(firstName);
        return user;
    }
}