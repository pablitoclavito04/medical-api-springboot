package com.example.medical.security;

import com.example.medical.dto.auth.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class JwtSecurityTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    // B1. POST /auth/login credenciales válidas → 200 y token
    @Test
    @DisplayName("B1: POST /auth/login con credenciales válidas → 200 + token")
    void login_validCredentials_returnsToken() throws Exception {
        LoginRequest req = new LoginRequest("admin@example.com", "Admin1234!");

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    // B2. Acceso sin token a endpoint protegido → 401 o 403
    @Test
    @DisplayName("B2: GET /patients sin token → acceso denegado (401 o 403)")
    void accessProtectedEndpoint_noToken_returnsUnauthorizedOrForbidden() throws Exception {
        mvc.perform(get("/patients"))
                .andExpect(status().is4xxClientError()); // Acepta 401 o 403
    }

    // B3. Acceso con rol incorrecto → 403
    @Test
    @DisplayName("B3: PACIENTE intenta DELETE /patients/{id} → 403")
    void accessWithWrongRole_returns403() throws Exception {
        String token = obtenerToken("pat@example.com", "Pat1234!");

        mvc.perform(delete("/patients/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // B4. Acceso con rol permitido → 200
    @Test
    @DisplayName("B4: RECEPCIONISTA accede a GET /patients → 200")
    void accessWithCorrectRole_returns200() throws Exception {
        String token = obtenerToken("recep@example.com", "Recep1234!");

        mvc.perform(get("/patients")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private String obtenerToken(String email, String password) throws Exception {
        LoginRequest req = new LoginRequest(email, password);

        MvcResult result = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return om.readTree(response).get("token").asText();
    }
}