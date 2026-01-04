package com.example.medical.service;

import com.example.medical.domain.*;
import com.example.medical.dto.appointment.AppointmentCreateRequest;
import com.example.medical.error.ConflictException;
import com.example.medical.error.InvalidDataException;
import com.example.medical.error.NotFoundException;
import com.example.medical.repo.AppointmentRepository;
import com.example.medical.repo.DoctorRepository;
import com.example.medical.repo.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appts;

    @Mock
    private DoctorRepository doctors;

    @Mock
    private PatientRepository patients;

    @InjectMocks
    private AppointmentServiceImpl service;

    private Doctor doctor;
    private Patient patient;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now().plusDays(1);

        // Crear doctor mock
        doctor = new Doctor();
        doctor.setFirstName("Dr. Juan");
        doctor.setLastName("García");

        // Crear patient mock
        patient = new Patient();
        patient.setFirstName("Ana");
        patient.setLastName("López");
    }

    // C1. Crear cita con doctor inexistente → lanza NotFoundException
    @Test
    @DisplayName("C1: Crear cita con doctor inexistente → NotFoundException")
    void create_doctorNotFound_throwsNotFoundException() {
        AppointmentCreateRequest req = new AppointmentCreateRequest(
                999L, 1L, now, now.plusMinutes(30), "Consulta"
        );

        when(doctors.findById(999L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.create(req));
        assertEquals("Doctor not found", ex.getMessage());
    }

    // C2. Crear cita con paciente inexistente → lanza NotFoundException
    @Test
    @DisplayName("C2: Crear cita con paciente inexistente → NotFoundException")
    void create_patientNotFound_throwsNotFoundException() {
        AppointmentCreateRequest req = new AppointmentCreateRequest(
                1L, 999L, now, now.plusMinutes(30), "Consulta"
        );

        when(doctors.findById(1L)).thenReturn(Optional.of(doctor));
        when(patients.findById(999L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.create(req));
        assertEquals("Patient not found", ex.getMessage());
    }

    // C3. Crear cita con rango horario inválido → InvalidDataException
    @Test
    @DisplayName("C3: Crear cita con endAt <= startAt → InvalidDataException")
    void create_invalidTimeRange_throwsInvalidDataException() {
        AppointmentCreateRequest req = new AppointmentCreateRequest(
                1L, 1L, now, now.minusMinutes(30), "Consulta"
        );

        InvalidDataException ex = assertThrows(InvalidDataException.class, () -> service.create(req));
        assertEquals("endAt must be after startAt", ex.getMessage());
    }

    // C3 bis. Crear cita con mismo horario inicio y fin → InvalidDataException
    @Test
    @DisplayName("C3b: Crear cita con endAt == startAt → InvalidDataException")
    void create_sameStartAndEnd_throwsInvalidDataException() {
        AppointmentCreateRequest req = new AppointmentCreateRequest(
                1L, 1L, now, now, "Consulta"
        );

        InvalidDataException ex = assertThrows(InvalidDataException.class, () -> service.create(req));
        assertEquals("endAt must be after startAt", ex.getMessage());
    }

    // C4. Crear cita con tiempos nulos → InvalidDataException
    @Test
    @DisplayName("C4: Crear cita con startAt null → InvalidDataException")
    void create_nullTimes_throwsInvalidDataException() {
        AppointmentCreateRequest req = new AppointmentCreateRequest(
                1L, 1L, null, now.plusMinutes(30), "Consulta"
        );

        InvalidDataException ex = assertThrows(InvalidDataException.class, () -> service.create(req));
        assertEquals("startAt and endAt are required", ex.getMessage());
    }

    // C5. Crear cita con solape → ConflictException
    @Test
    @DisplayName("C5: Crear cita que solapa con otra del mismo médico → ConflictException")
    void create_overlappingAppointment_throwsConflictException() {
        AppointmentCreateRequest req = new AppointmentCreateRequest(
                1L, 1L, now, now.plusMinutes(30), "Consulta"
        );

        when(doctors.findById(1L)).thenReturn(Optional.of(doctor));
        when(patients.findById(1L)).thenReturn(Optional.of(patient));
        when(appts.existsOverlapping(eq(1L), any(), any(), eq(-1L))).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class, () -> service.create(req));
        assertEquals("Doctor already has an appointment in this time slot", ex.getMessage());
    }

    // Test adicional: Crear cita válida funciona correctamente
    @Test
    @DisplayName("Crear cita válida → devuelve AppointmentResponse")
    void create_validRequest_returnsResponse() {
        AppointmentCreateRequest req = new AppointmentCreateRequest(
                1L, 1L, now, now.plusMinutes(30), "Consulta"
        );

        Appointment saved = new Appointment();
        saved.setDoctor(doctor);
        saved.setPatient(patient);
        saved.setStartAt(req.startAt());
        saved.setEndAt(req.endAt());
        saved.setReason(req.reason());

        when(doctors.findById(1L)).thenReturn(Optional.of(doctor));
        when(patients.findById(1L)).thenReturn(Optional.of(patient));
        when(appts.existsOverlapping(eq(1L), any(), any(), eq(-1L))).thenReturn(false);
        when(appts.save(any())).thenReturn(saved);

        var response = service.create(req);

        assertNotNull(response);
        assertEquals("Consulta", response.reason());
        verify(appts).save(any());
    }
}