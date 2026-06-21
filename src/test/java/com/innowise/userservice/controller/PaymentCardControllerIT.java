package com.innowise.userservice.controller;

import com.innowise.userservice.dto.PaymentCardCreateDto;
import com.innowise.userservice.dto.PaymentCardUpdateDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.repository.PaymentCardRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Testcontainers
public class PaymentCardControllerIT {
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
    private PaymentCardRepository paymentCardRepository;
    @Autowired
    private UserRepository userRepository;

    private static final String URL = "/api/cards";

    private static final String NAME = "testName";
    private static final String SURNAME = "testSurname";
    private static final String EMAIL = "test@test.com";
    private static final LocalDate BIRTHDATE = LocalDate.of(2000, 1, 1);

    private static final String NUMBER = "1234 5678 9012 3456";
    private static final String HOLDER = "testname testsurname";
    private static final String NUMBER_2 = "9876543210987654";
    private static final String EXPIRATION_DATE = "12/99";

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
        paymentCardRepository.deleteAll();
    }

    @Test
    void shouldCreateCard() throws Exception {
        String token = getAdminAccessToken();

        User user = createUserInDb(UUID.randomUUID());
        PaymentCardCreateDto createDto = getPaymentCardCreateDto(user.getId());

        mockMvc.perform(post(URL)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.number").value(NUMBER))
                .andExpect(jsonPath("$.holder").value(HOLDER))
                .andExpect(jsonPath("$.expirationDate").value(EXPIRATION_DATE))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andDo(print());
    }

    @Test
    void shouldRejectCreatingWhenNumberAlreadyExists() throws Exception {
        String token = getAdminAccessToken();
        User user = createUserInDb(UUID.randomUUID());
        createCardInDb(user.getId());

        PaymentCardCreateDto createDto = getPaymentCardCreateDto(user.getId());

        mockMvc.perform(post(URL)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
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
        String token = getAdminAccessToken();
        PaymentCardCreateDto createDto = new PaymentCardCreateDto(null, "", null, UUID.randomUUID());

        mockMvc.perform(post(URL)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.path").value(URL))
                .andExpect(jsonPath("$.dateTime").isNotEmpty())
                .andDo(print());
    }

    @Test
    void shouldRejectCreatingWhenCardLimitExceeded() throws Exception {
        String token = getAdminAccessToken();
        User user = createUserInDb(UUID.randomUUID());
        for (int i = 0; i < 5; i++) {
            createCardInDb(user.getId(), "999999999" + i);
        }

        PaymentCardCreateDto dto = getNextPaymentCardCreateDto(user.getId());

        mockMvc.perform(post(URL)
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
    void shouldGetCardById() throws Exception {
        String token = getAdminAccessToken();

        User user = createUserInDb(UUID.randomUUID());
        PaymentCard card = createCardInDb(user.getId());

        mockMvc.perform(get(URL + "/" + card.getId())
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.number").value(NUMBER))
                .andExpect(jsonPath("$.holder").value(HOLDER))
                .andExpect(jsonPath("$.expirationDate").value(EXPIRATION_DATE))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andDo(print());
    }

    @Test
    void shouldRejectGettingCardWhenCardNotFound() throws Exception {
        String token = getAdminAccessToken();

        mockMvc.perform(get(URL + "/" + 111L)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.path").value(URL + "/" + 111L))
                .andExpect(jsonPath("$.dateTime").isNotEmpty())
                .andDo(print());
    }

    @Test
    void shouldRejectGettingCardWhenIdIsInvalid() throws Exception {
        String token = getAdminAccessToken();

        mockMvc.perform(get(URL + "/" + 0)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.path").value(URL + "/" + 0))
                .andExpect(jsonPath("$.dateTime").isNotEmpty())
                .andDo(print());
    }

    @Test
    void shouldGetPagedCards() throws Exception {
        String token = getAdminAccessToken();

        User user = createUserInDb(UUID.randomUUID());
        createCardInDb(user.getId());
        createCardInDb(user.getId(), NUMBER_2);
        createCardInDb(user.getId(), "1111111111");

        mockMvc.perform(get(URL)
                        .header(
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
    void shouldGetUserCards() throws Exception {
        String token = getUserAccessToken();
        String id = extractSubject(token);

        User user = createUserInDb(UUID.fromString(id));
        createCardInDb(user.getId());
        createCardInDb(user.getId(), NUMBER_2);

        mockMvc.perform(get(URL + "/user")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].number").value(NUMBER))
                .andExpect(jsonPath("$[1].number").value(NUMBER_2))
                .andDo(print());
    }

    @Test
    void shouldUpdateCard() throws Exception {
        String token = getAdminAccessToken();

        User user = createUserInDb(UUID.randomUUID());
        PaymentCard card = createCardInDb(user.getId());
        PaymentCardUpdateDto dto = getPaymentCardUpdateDto(NUMBER_2);

        mockMvc.perform(patch(URL + "/" + user.getId() + "/" + card.getId())
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.number").value(NUMBER_2))
                .andExpect(jsonPath("$.holder").value(HOLDER))
                .andExpect(jsonPath("$.expirationDate").value(EXPIRATION_DATE))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andDo(print());

        PaymentCard updated = paymentCardRepository.findById(card.getId()).orElseThrow();
        assertEquals(NUMBER_2, updated.getNumber());
    }

    @Test
    void shouldRejectUpdatingWhenCardNotFound() throws Exception {
        String token = getAdminAccessToken();
        PaymentCardUpdateDto dto = getPaymentCardUpdateDto(NUMBER);

        mockMvc.perform(patch(URL + "/" + UUID.randomUUID() + "/" + 111L)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.path").value(URL + "/" + 111L))
                .andExpect(jsonPath("$.dateTime").isNotEmpty())
                .andDo(print());
    }

    @Test
    void shouldRejectUpdatingWhenNumberAlreadyExists() throws Exception {
        String token = getAdminAccessToken();
        User user = createUserInDb(UUID.randomUUID());
        PaymentCard card = createCardInDb(user.getId());
        PaymentCardUpdateDto updateDto = getPaymentCardUpdateDto(NUMBER);

        mockMvc.perform(patch(URL + "/" + user.getId() + "/" + card.getId())
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.path").value(URL + "/" + card.getId()))
                .andExpect(jsonPath("$.dateTime").isNotEmpty())
                .andDo(print());
    }

    @Test
    void shouldActivateCard() throws Exception {
        String token = getAdminAccessToken();
        User user = createUserInDb(UUID.randomUUID());
        PaymentCard card = createCardInDb(user.getId());
        card.setActive(false);
        paymentCardRepository.save(card);

        mockMvc.perform(patch(URL + "/" + user.getId() + "/" + card.getId() + "/activate")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isOk())
                .andDo(print());

        PaymentCard activated = paymentCardRepository.findById(card.getId()).orElseThrow();
        assertTrue(activated.getActive());
    }

    @Test
    void shouldRejectActivatingCardWhenCardAlreadyActive() throws Exception {
        String token = getAdminAccessToken();
        User user = createUserInDb(UUID.randomUUID());
        PaymentCard card = createCardInDb(user.getId(), NUMBER);

        mockMvc.perform(patch(URL + "/" + user.getId() + "/" + card.getId() + "/activate")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.path").value(URL + "/" + card.getId() + "/activate"))
                .andExpect(jsonPath("$.dateTime").isNotEmpty())
                .andDo(print());
    }

    @Test
    void shouldRejectActivatingCardWhenCardNotFound() throws Exception {
        String token = getAdminAccessToken();
        mockMvc.perform(patch(URL + "/" + UUID.randomUUID() + "/" + 111L + "/activate")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.path").value(URL + "/" + 111L + "/activate"))
                .andExpect(jsonPath("$.dateTime").isNotEmpty())
                .andDo(print());
    }


    @Test
    void shouldDeactivateCard() throws Exception {
        String token = getAdminAccessToken();
        User user = createUserInDb(UUID.randomUUID());
        PaymentCard card = createCardInDb(user.getId());

        mockMvc.perform(patch(URL + "/" + user.getId() + "/" + card.getId() + "/deactivate")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isOk())
                .andDo(print());

        PaymentCard deactivated = paymentCardRepository.findById(card.getId()).orElseThrow();
        assertFalse(deactivated.getActive());
    }

    @Test
    void shouldRejectDeactivatingCardWhenCardAlreadyInactive() throws Exception {
        String token = getAdminAccessToken();
        User user = createUserInDb(UUID.randomUUID());
        PaymentCard card = createCardInDb(user.getId(), NUMBER);
        card.setActive(false);
        paymentCardRepository.save(card);

        mockMvc.perform(patch(URL + "/" + user.getId() + "/" + card.getId() + "/deactivate")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.path").value(URL + "/" + card.getId() + "/deactivate"))
                .andExpect(jsonPath("$.dateTime").isNotEmpty())
                .andDo(print());
    }

    @Test
    void shouldRejectDeactivatingCardWhenCardNotFound() throws Exception {
        String token = getAdminAccessToken();
        mockMvc.perform(patch(URL + "/" + UUID.randomUUID() + "/" + 111L + "/deactivate")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.path").value(URL + "/" + 111L + "/deactivate"))
                .andExpect(jsonPath("$.dateTime").isNotEmpty())
                .andDo(print());
    }

    private User createUserInDb(UUID id) {
        User user = new User();

        user.setId(id);
        user.setName(NAME);
        user.setSurname(SURNAME);
        user.setBirthDate(BIRTHDATE);
        user.setEmail(EMAIL);
        user.setActive(true);

        return userRepository.save(user);
    }

    private PaymentCard createCardInDb(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();

        PaymentCard card = new PaymentCard();
        card.setNumber(NUMBER);
        card.setHolder(HOLDER);
        card.setExpirationDate(EXPIRATION_DATE);
        card.setUser(user);
        card.setActive(true);

        return paymentCardRepository.save(card);
    }

    private PaymentCard createCardInDb(UUID userId, String number) {
        User user = userRepository.findById(userId).orElseThrow();

        PaymentCard card = new PaymentCard();
        card.setNumber(number);
        card.setHolder(HOLDER);
        card.setExpirationDate(EXPIRATION_DATE);
        card.setUser(user);
        card.setActive(true);

        return paymentCardRepository.save(card);
    }

    private PaymentCardCreateDto getPaymentCardCreateDto(UUID userId) {
        return new PaymentCardCreateDto(
                NUMBER,
                HOLDER,
                EXPIRATION_DATE,
                userId
        );
    }

    private PaymentCardCreateDto getNextPaymentCardCreateDto(UUID userId) {
        return new PaymentCardCreateDto(
                NUMBER_2,
                HOLDER,
                EXPIRATION_DATE,
                userId
        );
    }

    private PaymentCardUpdateDto getPaymentCardUpdateDto(String number) {
        return new PaymentCardUpdateDto(
                number,
                HOLDER,
                EXPIRATION_DATE
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