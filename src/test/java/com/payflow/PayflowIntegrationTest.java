package com.payflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.auth.dto.RegisterRequest;
import com.payflow.wallet.dto.DepositRequest;
import com.payflow.transfer.dto.TransferRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class PayflowIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("payflow_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void fullFlow_register_login_deposit_transfer_history() throws Exception {
        RegisterRequest sender = new RegisterRequest();
        sender.setUsername("alice");
        sender.setEmail("alice@example.com");
        sender.setPassword("password123");

        RegisterRequest receiver = new RegisterRequest();
        receiver.setUsername("bob");
        receiver.setEmail("bob@example.com");
        receiver.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(receiver)))
                .andExpect(status().isOk());

        String senderResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sender)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String accessToken = objectMapper.readTree(senderResponse).get("accessToken").asText();

        DepositRequest deposit = new DepositRequest();
        deposit.setAmount(new BigDecimal("1000.00"));
        deposit.setDescription("initial funds");

        mockMvc.perform(post("/api/wallet/deposit")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(deposit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value(1000.00));

        TransferRequest transfer = new TransferRequest();
        transfer.setRecipient("bob");
        transfer.setAmount(new BigDecimal("300.00"));
        transfer.setDescription("payment");

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Idempotency-Key", "test-key-001")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value(300.00));

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Idempotency-Key", "test-key-001")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/wallet")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(700.00));

        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }
}