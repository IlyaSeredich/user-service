//package com.innowise.userservice.controller;
//
//import com.innowise.userservice.dto.PaymentCardCreateDto;
//import com.innowise.userservice.dto.PaymentCardUpdateDto;
//import com.innowise.userservice.entity.PaymentCard;
//import com.innowise.userservice.entity.User;
//import com.innowise.userservice.repository.PaymentCardRepository;
//import com.innowise.userservice.repository.UserRepository;
//import jakarta.transaction.Transactional;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
//import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.test.web.servlet.MockMvc;
//import org.testcontainers.containers.GenericContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import org.testcontainers.postgresql.PostgreSQLContainer;
//import tools.jackson.databind.ObjectMapper;
//
//import java.time.LocalDate;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@Transactional
//@ActiveProfiles("test")
//@Testcontainers
//public class PaymentCardControllerITTest {
//    @Container
//    @ServiceConnection
//    private static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:16")
//            .withDatabaseName("test-db")
//            .withUsername("test-user")
//            .withPassword("test-password");
//
//    @Container
//    @SuppressWarnings("resource")
//    private static final GenericContainer<?> redisContainer =
//            new GenericContainer<>("redis:7")
//                    .withExposedPorts(6379);
//
//    @DynamicPropertySource
//    private static void sourceProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.data.redis.host",
//                () -> "localhost");
//        registry.add("spring.data.redis.port",
//                () -> redisContainer.getMappedPort(6379));
//    }
//
//    @Autowired
//    private ObjectMapper objectMapper;
//    @Autowired
//    private MockMvc mockMvc;
//    @Autowired
//    private PaymentCardRepository paymentCardRepository;
//    @Autowired
//    private UserRepository userRepository;
//
//    private static final String URL = "/api/cards";
//
//    private static final String NAME = "testName";
//    private static final String SURNAME = "testSurname";
//    private static final String EMAIL = "test@test.com";
//    private static final LocalDate BIRTHDATE = LocalDate.of(2000, 1, 1);
//
//    private static final String NUMBER = "1234 5678 9012 3456";
//    private static final String HOLDER = "testname testsurname";
//    private static final String NUMBER_2 = "9876543210987654";
//    private static final String EXPIRATION_DATE = "12/99";
//
//    @Test
//    void shouldCreateCard() throws Exception {
//        User user = createUserInDb();
//        PaymentCardCreateDto createDto = getPaymentCardCreateDto(user.getId());
//
//        mockMvc.perform(post(URL)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(createDto)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id").isNumber())
//                .andExpect(jsonPath("$.number").value(NUMBER))
//                .andExpect(jsonPath("$.holder").value(HOLDER))
//                .andExpect(jsonPath("$.expirationDate").value(EXPIRATION_DATE))
//                .andExpect(jsonPath("$.active").value(true))
//                .andExpect(jsonPath("$.userId").value(user.getId()))
//                .andDo(print());
//    }
//
//    @Test
//    void shouldRejectCreatingWhenNumberAlreadyExists() throws Exception {
//        User user = createUserInDb();
//        createCardInDb(user.getId());
//
//        PaymentCardCreateDto createDto = getPaymentCardCreateDto(user.getId());
//
//        mockMvc.perform(post(URL)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(createDto)))
//                .andExpect(status().isConflict())
//                .andExpect(jsonPath("$.message").isNotEmpty())
//                .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.getReasonPhrase()))
//                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
//                .andExpect(jsonPath("$.path").value(URL))
//                .andExpect(jsonPath("$.dateTime").isNotEmpty())
//                .andDo(print());
//    }
//
//    @Test
//    void shouldRejectCreatingWhenValidationFails() throws Exception {
//        PaymentCardCreateDto createDto = new PaymentCardCreateDto(null, "", null, 111L);
//
//        mockMvc.perform(post(URL)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(createDto)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.message").isNotEmpty())
//                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
//                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
//                .andExpect(jsonPath("$.path").value(URL))
//                .andExpect(jsonPath("$.dateTime").isNotEmpty())
//                .andDo(print());
//    }
//
//    @Test
//    void shouldRejectCreatingWhenCardLimitExceeded() throws Exception {
//        User user = createUserInDb();
//        for (int i = 0; i < 5; i++) {
//            createCardInDb(user.getId(), "999999999" + i);
//        }
//
//        PaymentCardCreateDto dto = getNextPaymentCardCreateDto(user.getId());
//
//        mockMvc.perform(post(URL)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(dto)))
//                .andExpect(status().isConflict())
//                .andExpect(jsonPath("$.message").isNotEmpty())
//                .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.getReasonPhrase()))
//                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
//                .andExpect(jsonPath("$.path").value(URL))
//                .andExpect(jsonPath("$.dateTime").isNotEmpty())
//                .andDo(print());
//    }
//
//
//    @Test
//    void shouldGetCardById() throws Exception {
//        User user = createUserInDb();
//        PaymentCard card = createCardInDb(user.getId());
//
//        mockMvc.perform(get(URL + "/" + card.getId()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").isNumber())
//                .andExpect(jsonPath("$.number").value(NUMBER))
//                .andExpect(jsonPath("$.holder").value(HOLDER))
//                .andExpect(jsonPath("$.expirationDate").value(EXPIRATION_DATE))
//                .andExpect(jsonPath("$.active").value(true))
//                .andExpect(jsonPath("$.userId").value(user.getId()))
//                .andDo(print());
//    }
//
//    @Test
//    void shouldRejectGettingCardWhenCardNotFound() throws Exception {
//        mockMvc.perform(get(URL + "/" + 111L))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.message").isNotEmpty())
//                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
//                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
//                .andExpect(jsonPath("$.path").value(URL + "/" + 111L))
//                .andExpect(jsonPath("$.dateTime").isNotEmpty())
//                .andDo(print());
//    }
//
//    @Test
//    void shouldRejectGettingCardWhenIdIsInvalid() throws Exception {
//        mockMvc.perform(get(URL + "/" + 0))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.message").isNotEmpty())
//                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
//                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
//                .andExpect(jsonPath("$.path").value(URL + "/" + 0))
//                .andExpect(jsonPath("$.dateTime").isNotEmpty())
//                .andDo(print());
//    }
//
//    @Test
//    void shouldGetPagedCards() throws Exception {
//        User user = createUserInDb();
//        createCardInDb(user.getId());
//        createCardInDb(user.getId(), NUMBER_2);
//        createCardInDb(user.getId(), "1111111111");
//
//        mockMvc.perform(get(URL))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content").isArray())
//                .andExpect(jsonPath("$.content.length()").value(3))
//                .andExpect(jsonPath("$.pageNumber").value(0))
//                .andExpect(jsonPath("$.pageSize").value(10))
//                .andExpect(jsonPath("$.totalElements").value(3))
//                .andExpect(jsonPath("$.totalPages").value(1))
//                .andDo(print());
//    }
//
//    @Test
//    void shouldGetUserCards() throws Exception {
//        User user = createUserInDb();
//        createCardInDb(user.getId());
//        createCardInDb(user.getId(), NUMBER_2);
//
//        mockMvc.perform(get(URL + "/user/" + user.getId()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$").isArray())
//                .andExpect(jsonPath("$.length()").value(2))
//                .andExpect(jsonPath("$[0].number").value(NUMBER))
//                .andExpect(jsonPath("$[1].number").value(NUMBER_2))
//                .andDo(print());
//    }
//
//    @Test
//    void shouldUpdateCard() throws Exception {
//        User user = createUserInDb();
//        PaymentCard card = createCardInDb(user.getId());
//        PaymentCardUpdateDto dto = getPaymentCardUpdateDto(NUMBER_2);
//
//        mockMvc.perform(patch(URL + "/" + card.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(dto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").isNumber())
//                .andExpect(jsonPath("$.number").value(NUMBER_2))
//                .andExpect(jsonPath("$.holder").value(HOLDER))
//                .andExpect(jsonPath("$.expirationDate").value(EXPIRATION_DATE))
//                .andExpect(jsonPath("$.active").value(true))
//                .andExpect(jsonPath("$.userId").value(user.getId()))
//                .andDo(print());
//
//        PaymentCard updated = paymentCardRepository.findById(card.getId()).orElseThrow();
//        assertEquals(NUMBER_2, updated.getNumber());
//    }
//
//    @Test
//    void shouldRejectUpdatingWhenCardNotFound() throws Exception {
//        PaymentCardUpdateDto dto = getPaymentCardUpdateDto(NUMBER);
//
//        mockMvc.perform(patch(URL + "/" + 111L)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(dto)))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.message").isNotEmpty())
//                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
//                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
//                .andExpect(jsonPath("$.path").value(URL + "/" + 111L))
//                .andExpect(jsonPath("$.dateTime").isNotEmpty())
//                .andDo(print());
//    }
//
//    @Test
//    void shouldRejectUpdatingWhenNumberAlreadyExists() throws Exception {
//        User user = createUserInDb();
//        PaymentCard card = createCardInDb(user.getId());
//        PaymentCardUpdateDto updateDto = getPaymentCardUpdateDto(NUMBER);
//
//        mockMvc.perform(patch(URL + "/" + card.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateDto)))
//                .andExpect(status().isConflict())
//                .andExpect(jsonPath("$.message").isNotEmpty())
//                .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.getReasonPhrase()))
//                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
//                .andExpect(jsonPath("$.path").value(URL + "/" + card.getId()))
//                .andExpect(jsonPath("$.dateTime").isNotEmpty())
//                .andDo(print());
//    }
//
//    @Test
//    void shouldActivateCard() throws Exception {
//        User user = createUserInDb();
//        PaymentCard card = createCardInDb(user.getId());
//        card.setActive(false);
//        paymentCardRepository.save(card);
//
//        mockMvc.perform(patch(URL + "/" + card.getId() + "/activate"))
//                .andExpect(status().isOk())
//                .andDo(print());
//
//        PaymentCard activated = paymentCardRepository.findById(card.getId()).orElseThrow();
//        assertTrue(activated.getActive());
//    }
//
//    @Test
//    void shouldRejectActivatingCardWhenCardAlreadyActive() throws Exception {
//        User user = createUserInDb();
//        PaymentCard card = createCardInDb(user.getId(), NUMBER);
//
//        mockMvc.perform(patch(URL + "/" + card.getId() + "/activate"))
//                .andExpect(status().isConflict())
//                .andExpect(jsonPath("$.message").isNotEmpty())
//                .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.getReasonPhrase()))
//                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
//                .andExpect(jsonPath("$.path").value(URL + "/" + card.getId() + "/activate"))
//                .andExpect(jsonPath("$.dateTime").isNotEmpty())
//                .andDo(print());
//    }
//
//    @Test
//    void shouldRejectActivatingCardWhenCardNotFound() throws Exception {
//        mockMvc.perform(patch(URL + "/" + 111L + "/activate"))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.message").isNotEmpty())
//                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
//                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
//                .andExpect(jsonPath("$.path").value(URL + "/" + 111L + "/activate"))
//                .andExpect(jsonPath("$.dateTime").isNotEmpty())
//                .andDo(print());
//    }
//
//
//    @Test
//    void shouldDeactivateCard() throws Exception {
//        User user = createUserInDb();
//        PaymentCard card = createCardInDb(user.getId());
//
//        mockMvc.perform(patch(URL + "/" + card.getId() + "/deactivate"))
//                .andExpect(status().isOk())
//                .andDo(print());
//
//        PaymentCard deactivated = paymentCardRepository.findById(card.getId()).orElseThrow();
//        assertFalse(deactivated.getActive());
//    }
//
//    @Test
//    void shouldRejectDeactivatingCardWhenCardAlreadyInactive() throws Exception {
//        User user = createUserInDb();
//        PaymentCard card = createCardInDb(user.getId(), NUMBER);
//        card.setActive(false);
//        paymentCardRepository.save(card);
//
//        mockMvc.perform(patch(URL + "/" + card.getId() + "/deactivate"))
//                .andExpect(status().isConflict())
//                .andExpect(jsonPath("$.message").isNotEmpty())
//                .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.getReasonPhrase()))
//                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
//                .andExpect(jsonPath("$.path").value(URL + "/" + card.getId() + "/deactivate"))
//                .andExpect(jsonPath("$.dateTime").isNotEmpty())
//                .andDo(print());
//    }
//
//    @Test
//    void shouldRejectDeactivatingCardWhenCardNotFound() throws Exception {
//        mockMvc.perform(patch(URL + "/" + 111L + "/deactivate"))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.message").isNotEmpty())
//                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
//                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
//                .andExpect(jsonPath("$.path").value(URL + "/" + 111L + "/deactivate"))
//                .andExpect(jsonPath("$.dateTime").isNotEmpty())
//                .andDo(print());
//    }
//
//    private User createUserInDb() {
//        User user = new User();
//
//        user.setName(NAME);
//        user.setSurname(SURNAME);
//        user.setBirthDate(BIRTHDATE);
//        user.setEmail(EMAIL);
//        user.setActive(true);
//
//        return userRepository.save(user);
//    }
//
//    private PaymentCard createCardInDb(Long userId) {
//        User user = userRepository.findById(userId).orElseThrow();
//
//        PaymentCard card = new PaymentCard();
//        card.setNumber(NUMBER);
//        card.setHolder(HOLDER);
//        card.setExpirationDate(EXPIRATION_DATE);
//        card.setUser(user);
//        card.setActive(true);
//
//        return paymentCardRepository.save(card);
//    }
//
//    private PaymentCard createCardInDb(Long userId, String number) {
//        User user = userRepository.findById(userId).orElseThrow();
//
//        PaymentCard card = new PaymentCard();
//        card.setNumber(number);
//        card.setHolder(HOLDER);
//        card.setExpirationDate(EXPIRATION_DATE);
//        card.setUser(user);
//        card.setActive(true);
//
//        return paymentCardRepository.save(card);
//    }
//
//    private PaymentCardCreateDto getPaymentCardCreateDto(Long userId) {
//        return new PaymentCardCreateDto(
//                NUMBER,
//                HOLDER,
//                EXPIRATION_DATE,
//                userId
//        );
//    }
//
//    private PaymentCardCreateDto getNextPaymentCardCreateDto(Long userId) {
//        return new PaymentCardCreateDto(
//                NUMBER_2,
//                HOLDER,
//                EXPIRATION_DATE,
//                userId
//        );
//    }
//
//    private PaymentCardUpdateDto getPaymentCardUpdateDto(String number) {
//        return new PaymentCardUpdateDto(
//                number,
//                HOLDER,
//                EXPIRATION_DATE
//        );
//    }
//
//    private Jwt mockJwt(String userId) {
//        return Jwt.withTokenValue("token")
//                .claim("sub", userId)
//                .header("alg", "none")
//                .build();
//    }
//}