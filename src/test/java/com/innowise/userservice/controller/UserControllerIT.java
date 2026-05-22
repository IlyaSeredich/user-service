package com.innowise.userservice.controller;

import com.innowise.userservice.dto.UserCreateDto;
import com.innowise.userservice.dto.UserUpdateDto;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Testcontainers
class UserControllerIT {
    @Container
    @ServiceConnection
    private static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:16")
            .withDatabaseName("test-db")
            .withUsername("test-user")
            .withPassword("test-password");

    @Container
    @SuppressWarnings("resource")
    private static final GenericContainer<?> redisContainer =
            new GenericContainer<>("redis:7")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    private static void sourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host",
                () -> "localhost");
        registry.add("spring.data.redis.port",
                () -> redisContainer.getMappedPort(6379));
    }
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    private static final String URL = "/api/users";
    private static final String NAME = "testName";
    private static final String NAME_2 = "test2Name";
    private static final String SURNAME = "testSurname";
    private static final String SURNAME_2 = "test2Surname";
    private static final String EMAIL = "test@test.com";
    private static final String EMAIL_2 = "test2@test.com";
    private static final LocalDate BIRTHDATE = LocalDate.of(2000, 1, 1);


    @Test
    void shouldCreateUser() throws Exception {
        UserCreateDto userCreateDto = getUserCreateDto();

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(NAME))
                .andExpect(jsonPath("$.surname").value(SURNAME))
                .andExpect(jsonPath("$.birthDate").isNotEmpty())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.active").value(true))
                .andDo(print());
    }

    @Test
    void shouldRejectCreatingWhenEmailAlreadyExists() throws Exception {
        createUserInDb(EMAIL);

        UserCreateDto userCreateDto = new UserCreateDto(
                NAME_2,
                SURNAME_2,
                BIRTHDATE,
                EMAIL
        );

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.path").value(URL))
                .andExpect(jsonPath("$.dateTime").isNotEmpty())
                .andDo(print());
    }

    @Test
    void shouldRejectCreatingWhenValidationFails() throws Exception {
        UserCreateDto dto = new UserCreateDto("", "", null, null);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.path").value(URL))
                .andExpect(jsonPath("$.dateTime").isNotEmpty())
                .andDo(print());
    }

    @Test
    void shouldGetUserById() throws Exception {
        User user = createUserInDb(EMAIL);

        mockMvc.perform(get(URL + "/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.name").value(NAME))
                .andExpect(jsonPath("$.surname").value(SURNAME))
                .andExpect(jsonPath("$.birthDate").isNotEmpty())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.active").value(true))
                .andDo(print());
    }

    @Test
    void shouldRejectGettingUserWhenUserNotFound() throws Exception {
        mockMvc.perform(get(URL + "/" + 111L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.path").value(URL + "/" + 111L))
                .andExpect(jsonPath("$.dateTime").isNotEmpty())
                .andDo(print());
    }

    @Test
    void shouldRejectGettingUserWhenIdIsInvalid() throws Exception {
        mockMvc.perform(get(URL + "/" + 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.path").value(URL + "/" + 0))
                .andExpect(jsonPath("$.dateTime").isNotEmpty())
                .andDo(print());
    }

    @Test
    void shouldGetPagedUsers() throws Exception {
        createUserInDb(EMAIL);
        createUserInDb(EMAIL_2);
        createUserInDb("test3@test.com");

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andDo(print());
    }

    @Test
    void shouldFilterUsers() throws Exception {
        createUserInDb(EMAIL);

        User user = new User();
        user.setName(NAME_2);
        user.setSurname(SURNAME);
        user.setBirthDate(BIRTHDATE);
        user.setEmail(EMAIL_2);
        user.setActive(true);

        userRepository.save(user);

        mockMvc.perform(get(URL)
                        .param("name", NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value(NAME))
                .andDo(print());
    }

    @Test
    void shouldUpdateUser() throws Exception {
        User user = createUserInDb(EMAIL);
        UserUpdateDto userUpdateDto = getUserUpdateDto(EMAIL_2);

        mockMvc.perform(patch(URL + "/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.name").value(NAME))
                .andExpect(jsonPath("$.surname").value(SURNAME))
                .andExpect(jsonPath("$.birthDate").isNotEmpty())
                .andExpect(jsonPath("$.email").value(EMAIL_2))
                .andExpect(jsonPath("$.active").value(true))
                .andDo(print());

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(EMAIL_2, updatedUser.getEmail());
    }

    @Test
    void shouldRejectUpdatingWhenUserNotFound() throws Exception {
        UserUpdateDto userUpdateDto = getUserUpdateDto(EMAIL);
        mockMvc.perform(patch(URL + "/" + 111L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.path").value(URL + "/" + 111L))
                .andExpect(jsonPath("$.dateTime").isNotEmpty())
                .andDo(print());
    }

    @Test
    void shouldRejectUpdatingWhenEmailAlreadyExists() throws Exception {
        User user = createUserInDb(EMAIL);
        createUserInDb(EMAIL_2);

        UserUpdateDto dto = getUserUpdateDto(EMAIL_2);

        mockMvc.perform(patch(URL + "/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.path").value(URL + "/" + user.getId()))
                .andExpect(jsonPath("$.dateTime").isNotEmpty())
                .andDo(print());
    }

    @Test
    void shouldRejectActivatingUserWhenUserAlreadyActive() throws Exception {
        User user = createUserInDb(EMAIL);

        mockMvc.perform(patch(URL + "/" + user.getId() + "/activate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.path").value(URL + "/" + user.getId() + "/activate"))
                .andExpect(jsonPath("$.dateTime").isNotEmpty())
                .andDo(print());
    }

    @Test
    void shouldRejectActivatingUserWhenUserNotFound() throws Exception {
        mockMvc.perform(patch(URL + "/" + 111L + "/activate"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.path").value(URL + "/" + 111L + "/activate"))
                .andExpect(jsonPath("$.dateTime").isNotEmpty())
                .andDo(print());
    }

    @Test
    void shouldDeactivateUser() throws Exception {
        User user = createUserInDb(EMAIL);

        mockMvc.perform(patch(URL + "/" + user.getId() + "/deactivate"))
                .andExpect(status().isOk());

        User deactivatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertFalse(deactivatedUser.getActive());
    }

    @Test
    void shouldRejectDeactivatingUserWhenUserAlreadyInactive() throws Exception {
        User user = createUserInDb(EMAIL);

        mockMvc.perform(patch(URL + "/" + user.getId() + "/deactivate"));

        mockMvc.perform(patch(URL + "/" + user.getId() + "/deactivate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.path").value(URL + "/" + user.getId() + "/deactivate"))
                .andExpect(jsonPath("$.dateTime").isNotEmpty())
                .andDo(print());
    }

    @Test
    void  shouldRejectDeactivatingUserWhenUserNotFound() throws Exception {
        mockMvc.perform(patch(URL + "/" + 111L + "/deactivate"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.path").value(URL + "/" + 111L + "/deactivate"))
                .andExpect(jsonPath("$.dateTime").isNotEmpty())
                .andDo(print());
    }

    private User createUserInDb(String email) {
        User user = new User();
        user.setName(NAME);
        user.setSurname(SURNAME);
        user.setBirthDate(BIRTHDATE);
        user.setEmail(email);
        user.setActive(true);

        return userRepository.save(user);
    }

    private UserCreateDto getUserCreateDto() {
        return new UserCreateDto(
                NAME,
                SURNAME,
                BIRTHDATE,
                EMAIL
        );
    }

    private UserUpdateDto getUserUpdateDto(String email) {
        return new UserUpdateDto(
                NAME,
                SURNAME,
                BIRTHDATE,
                email
        );
    }
}