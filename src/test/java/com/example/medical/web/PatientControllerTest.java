package com.example.medical.web;

import com.example.medical.dto.patient.PatientCreateRequest;
import com.example.medical.dto.patient.PatientResponse;
import com.example.medical.error.NotFoundException;
import com.example.medical.service.PatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PatientControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @MockBean
    private PatientService service;

    // A1. POST /patients → 201 y Location correcto
    @Test
    @DisplayName("A1: POST /patients con DTO válido → 201 + Location")
    @WithMockUser(roles = "ADMIN")
    void createPatient_validDto_returns201WithLocation() throws Exception {
        PatientCreateRequest req = new PatientCreateRequest("12345678A", "Ana", "López", "600000000");
        PatientResponse res = new PatientResponse(1L, "12345678A", "Ana", "López", "600000000", true);

        when(service.create(any())).thenReturn(res);

        mvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/patients/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Ana"));
    }

    // A2. POST /patients DTO inválido → 400
    @Test
    @DisplayName("A2: POST /patients con DTO inválido → 400")
    @WithMockUser(roles = "ADMIN")
    void createPatient_invalidDto_returns400() throws Exception {
        PatientCreateRequest req = new PatientCreateRequest("12345678A", "", "López", "600000000");

        mvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    // A3. GET /patients/{id} inexistente → 404
    @Test
    @DisplayName("A3: GET /patients/{id} inexistente → 404")
    @WithMockUser(roles = "ADMIN")
    void getPatient_notFound_returns404() throws Exception {
        when(service.getById(999L)).thenThrow(new NotFoundException("Patient not found"));

        mvc.perform(get("/patients/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Patient not found"));
    }

    // A4. DELETE /patients/{id} existente → 204
    @Test
    @DisplayName("A4: DELETE /patients/{id} existente → 204")
    @WithMockUser(roles = "ADMIN")
    void deletePatient_exists_returns204() throws Exception {
        doNothing().when(service).delete(1L);

        mvc.perform(delete("/patients/1"))
                .andExpect(status().isNoContent());
    }

    // A5. PUT /patients/{id} existente → 200
    @Test
    @DisplayName("A5: PUT /patients/{id} existente → 200")
    @WithMockUser(roles = "ADMIN")
    void updatePatient_exists_returns200() throws Exception {
        PatientCreateRequest req = new PatientCreateRequest("12345678A", "Ana María", "López", "600000001");
        PatientResponse res = new PatientResponse(1L, "12345678A", "Ana María", "López", "600000001", true);

        when(service.update(eq(1L), any())).thenReturn(res);

        mvc.perform(put("/patients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ana María"));
    }
}