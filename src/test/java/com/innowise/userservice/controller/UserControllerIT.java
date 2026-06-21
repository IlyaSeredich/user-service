package com.innowise.userservice.controller;

import com.innowise.userservice.dto.UserCreateDto;
import com.innowise.userservice.dto.UserUpdateDto;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.repository.UserRepository;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
    protected static final KeycloakContainer keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:26.4.5")
            .withAdminUsername("admin")
            .withAdminPassword("admin")
            .withStartupTimeout(Duration.ofMinutes(5));

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
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> keycloakContainer.getAuthServerUrl() +
                        "/realms/test-realm/protocol/openid-connect/certs");
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


    private static final String REALM = "test-realm";
    private static final String USER_CLIENT = "user-test-client";
    private static final String ADMIN_CLIENT = "admin-test-client";
    private static final String SECRET = "test-secret";
    private static final String USER_ROLE = "user";
    private static final String ADMIN_ROLE = "admin";

    @BeforeAll
    static void setupKeycloak() {
        String authUrl = keycloakContainer.getAuthServerUrl();

        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(authUrl)
                .realm("master")
                .clientId("admin-cli")
                .username("admin")
                .password("admin")
                .build();

        RealmRepresentation realm = new RealmRepresentation();
        realm.setRealm(REALM);
        realm.setEnabled(true);
        keycloak.realms().create(realm);

        ClientRepresentation userClient = new ClientRepresentation();
        userClient.setClientId(USER_CLIENT);
        userClient.setStandardFlowEnabled(true);
        userClient.setPublicClient(false);
        userClient.setSecret(SECRET);
        userClient.setServiceAccountsEnabled(true);

        ClientRepresentation adminClient = new ClientRepresentation();
        adminClient.setClientId(ADMIN_CLIENT);
        adminClient.setStandardFlowEnabled(true);
        adminClient.setPublicClient(false);
        adminClient.setSecret(SECRET);
        adminClient.setServiceAccountsEnabled(true);

        Response userResponse = keycloak.realm(REALM).clients().create(userClient);
        Response adminResponse = keycloak.realm(REALM).clients().create(adminClient);

        String userId = CreatedResponseUtil.getCreatedId(userResponse);
        String adminId = CreatedResponseUtil.getCreatedId(adminResponse);

        UserRepresentation userServiceAccountUser = keycloak.realm(REALM)
                .clients()
                .get(userId)
                .getServiceAccountUser();

        UserRepresentation adminServiceAccountUser = keycloak.realm(REALM)
                .clients()
                .get(adminId)
                .getServiceAccountUser();

        RoleRepresentation userRole = new RoleRepresentation();
        userRole.setName(USER_ROLE);
        keycloak.realm(REALM).roles().create(userRole);

        RoleRepresentation adminRole = new RoleRepresentation();
        adminRole.setName(ADMIN_ROLE);
        keycloak.realm(REALM).roles().create(adminRole);

        RoleRepresentation userRoleRepresentation = keycloak.realm(REALM).roles()
                .get(USER_ROLE)
                .toRepresentation();

        RoleRepresentation adminRoleRepresentation = keycloak.realm(REALM).roles()
                .get(ADMIN_ROLE)
                .toRepresentation();

        keycloak.realm(REALM).users()
                .get(userServiceAccountUser.getId())
                .roles()
                .realmLevel()
                .add(List.of(userRoleRepresentation));

        keycloak.realm(REALM).users()
                .get(adminServiceAccountUser.getId())
                .roles()
                .realmLevel()
                .add(List.of(adminRoleRepresentation));
    }

    @BeforeEach
    void clearDb() {
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateUser() throws Exception {
        String token = getUserAccessToken();
        String id = extractSubject(token);
        UserCreateDto userCreateDto = getUserCreateDto(UUID.fromString(id));

        mockMvc.perform(post(URL)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id))
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

        String token = getUserAccessToken();
        String id = extractSubject(token);

        UserCreateDto userCreateDto = new UserCreateDto(
                UUID.fromString(id),
                NAME_2,
                SURNAME_2,
                BIRTHDATE,
                EMAIL
        );

        mockMvc.perform(post(URL)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
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
        UserCreateDto dto = new UserCreateDto(UUID.randomUUID(),"", "", null, null);

        String token = getUserAccessToken();

        mockMvc.perform(post(URL)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
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
        String token = getUserAccessToken();
        String id = extractSubject(token);

        User user = new User();
        user.setId(UUID.fromString(id));
        user.setName(NAME);
        user.setSurname(SURNAME);
        user.setBirthDate(BIRTHDATE);
        user.setEmail(EMAIL);
        user.setActive(true);

        userRepository.save(user);

        mockMvc.perform(get(URL + "/me").header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(NAME))
                .andExpect(jsonPath("$.surname").value(SURNAME))
                .andExpect(jsonPath("$.birthDate").isNotEmpty())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.active").value(true))
                .andDo(print());
    }


    @Test
    void shouldGetPagedUsers() throws Exception {
        createUserInDb(EMAIL);
        createUserInDb(EMAIL_2);
        createUserInDb("test3@test.com");

        String token = getAdminAccessToken();

        mockMvc.perform(get(URL).header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                ))
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

        String token = getAdminAccessToken();

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(NAME_2);
        user.setSurname(SURNAME);
        user.setBirthDate(BIRTHDATE);
        user.setEmail(EMAIL_2);
        user.setActive(true);

        userRepository.save(user);

        mockMvc.perform(get(URL).header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .param("name", NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value(NAME))
                .andDo(print());
    }

    @Test
    void shouldUpdateUser() throws Exception {
        UserUpdateDto userUpdateDto = getUserUpdateDto(EMAIL_2);

        String token = getUserAccessToken();
        String id = extractSubject(token);

        User user = new User();
        user.setId(UUID.fromString(id));
        user.setName(NAME);
        user.setSurname(SURNAME);
        user.setBirthDate(BIRTHDATE);
        user.setEmail(EMAIL);
        user.setActive(true);

        userRepository.save(user);

        mockMvc.perform(patch(URL).header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
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
    void shouldRejectUpdatingWhenEmailAlreadyExists() throws Exception {
        User user = createUserInDb(EMAIL);

        String token = getUserAccessToken();
        String id = extractSubject(token);

        User user2 = new User();
        user2.setId(UUID.fromString(id));
        user2.setName(NAME);
        user2.setSurname(SURNAME);
        user2.setBirthDate(BIRTHDATE);
        user2.setEmail(EMAIL_2);
        user2.setActive(true);

        userRepository.save(user2);

        UserUpdateDto dto = getUserUpdateDto(EMAIL);


        mockMvc.perform(patch(URL)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.path").value(URL))
                .andExpect(jsonPath("$.dateTime").isNotEmpty())
                .andDo(print());
    }

    @Test
    void shouldRejectActivatingUserWhenUserAlreadyActive() throws Exception {
        User user = createUserInDb(EMAIL);

        String token = getAdminAccessToken();

        mockMvc.perform(patch(URL + "/" + user.getId() + "/activate")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
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
        String token = getAdminAccessToken();

        mockMvc.perform(patch(URL + "/" + UUID.randomUUID() + "/activate")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.path").isNotEmpty())
                .andExpect(jsonPath("$.dateTime").isNotEmpty())
                .andDo(print());
    }

    @Test
    void shouldDeactivateUser() throws Exception {
        User user = createUserInDb(EMAIL);
        String token = getAdminAccessToken();

        mockMvc.perform(patch(URL + "/" + user.getId() + "/deactivate")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isOk());

        User deactivatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertFalse(deactivatedUser.getActive());
    }

    @Test
    void shouldRejectDeactivatingUserWhenUserAlreadyInactive() throws Exception {
        User user = createUserInDb(EMAIL);

        String token = getAdminAccessToken();

        mockMvc.perform(patch(URL + "/" + user.getId() + "/deactivate")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                ));

        mockMvc.perform(patch(URL + "/" + user.getId() + "/deactivate")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
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
        String token = getAdminAccessToken();

        mockMvc.perform(patch(URL + "/" + UUID.randomUUID() + "/deactivate")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.path").isNotEmpty())
                .andExpect(jsonPath("$.dateTime").isNotEmpty())
                .andDo(print());
    }

    private User createUserInDb(String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(NAME);
        user.setSurname(SURNAME);
        user.setBirthDate(BIRTHDATE);
        user.setEmail(email);
        user.setActive(true);

        return userRepository.save(user);
    }

    private UserCreateDto getUserCreateDto(UUID id) {
        return new UserCreateDto(
                id,
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

    private String getUserAccessToken() {
        String tokenUrl = keycloakContainer.getAuthServerUrl()
                + "/realms/test-realm/protocol/openid-connect/token";

        Map<String, String> params = new HashMap<>();
        params.put("realm", REALM);
        params.put("client_id", USER_CLIENT);
        params.put("client_secret", SECRET);
        params.put("grant_type", OAuth2Constants.CLIENT_CREDENTIALS);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = new RestTemplate().postForEntity(tokenUrl, request, Map.class);

        return (String) response.getBody().get("access_token");
    }

    private String extractSubject(String token) {
        String payload = token.split("\\.")[1];

        byte[] decoded = Base64.getUrlDecoder()
                .decode(payload);

        try {
            Map<String, Object> claims = objectMapper.readValue(decoded, Map.class);

            return (String) claims.get("sub");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getAdminAccessToken() {
        String tokenUrl = keycloakContainer.getAuthServerUrl()
                + "/realms/test-realm/protocol/openid-connect/token";

        Map<String, String> params = new HashMap<>();
        params.put("realm", "test-realm");
        params.put("client_id", "admin-test-client");
        params.put("client_secret", SECRET);
        params.put("grant_type", OAuth2Constants.CLIENT_CREDENTIALS);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = new RestTemplate().postForEntity(tokenUrl, request, Map.class);

        return (String) response.getBody().get("access_token");
    }
}