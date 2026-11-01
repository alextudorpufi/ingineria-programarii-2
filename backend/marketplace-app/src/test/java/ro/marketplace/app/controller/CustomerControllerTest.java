package ro.marketplace.app.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import ro.marketplace.app.security.SecurityConfig;
import ro.marketplace.application.service.CustomerService;
import ro.marketplace.domain.exception.ResourceNotFoundException;
import ro.marketplace.domain.model.Customer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CustomerController.class)
@Import(SecurityConfig.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Test
    @DisplayName("GET /api/customers returns customer list for an admin")
    void getAllCustomersReturnsList() throws Exception {
        UUID id = UUID.randomUUID();
        Customer customer = Customer.builder()
                .id(id)
                .name("Ion Popescu")
                .email("ion@example.com")
                .phone("+40712345678")
                .createdAt(LocalDateTime.now())
                .build();

        when(customerService.getAllCustomers()).thenReturn(List.of(customer));

        mockMvc.perform(get("/api/customers").with(adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].name").value("Ion Popescu"))
                .andExpect(jsonPath("$[0].email").value("ion@example.com"));
    }

    @Test
    @DisplayName("GET /api/customers/{id} returns 404 ProblemDetail when not found")
    void getCustomerByIdNotFoundReturnsProblemDetail() throws Exception {
        UUID id = UUID.randomUUID();
        when(customerService.getCustomerById(id))
                .thenThrow(new ResourceNotFoundException("Customer not found with id: " + id));

        mockMvc.perform(get("/api/customers/" + id).with(adminToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.detail").value("Customer not found with id: " + id));
    }

    @Test
    @DisplayName("POST /api/customers creates customer")
    void createCustomerSuccess() throws Exception {
        UUID id = UUID.randomUUID();
        Customer customer = Customer.builder()
                .id(id)
                .name("Ana Ionescu")
                .email("ana@example.com")
                .createdAt(LocalDateTime.now())
                .build();

        when(customerService.createCustomer(any(Customer.class))).thenReturn(customer);

        String json = """
                {
                    "name": "Ana Ionescu",
                    "email": "ana@example.com"
                }
                """;

        mockMvc.perform(post("/api/customers")
                        .with(adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Ana Ionescu"));
    }

    @Test
    @DisplayName("Data integrity violations map to 409 with a ProblemDetail")
    void dataIntegrityViolationMapsToConflict() throws Exception {
        when(customerService.createCustomer(any(Customer.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        mockMvc.perform(post("/api/customers")
                        .with(adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ana","email":"ana@example.com"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));
    }

    @Test
    @DisplayName("Optimistic locking failures map to 409 with a ProblemDetail")
    void optimisticLockMapsToConflict() throws Exception {
        when(customerService.createCustomer(any(Customer.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException("Customer", UUID.randomUUID()));

        mockMvc.perform(post("/api/customers")
                        .with(adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ana","email":"ana@example.com"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));
    }

    @Test
    @DisplayName("Customer endpoints reject non-admin tokens")
    void nonAdminIsForbidden() throws Exception {
        mockMvc.perform(get("/api/customers").with(token("PRO")))
                .andExpect(status().isForbidden());
    }

    private static RequestPostProcessor adminToken() {
        return token("ADMIN");
    }

    private static RequestPostProcessor token(String... roles) {
        List<GrantedAuthority> authorities = java.util.Arrays.stream(roles)
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        return jwt().jwt(jwt -> jwt
                        .subject(UUID.randomUUID().toString())
                        .claim("email", "admin@marketplace.ro")
                        .claim("roles", List.of(roles)))
                .authorities(authorities.toArray(GrantedAuthority[]::new));
    }
}
