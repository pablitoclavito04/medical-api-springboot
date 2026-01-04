package com.example.medical.repo;

import com.example.medical.domain.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AppointmentRepositoryTest {

    @Autowired
    private AppointmentRepository appts;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private EntityManager em;

    private Doctor doctor1;
    private Patient patient;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        baseTime = LocalDateTime.of(2025, 6, 15, 10, 0);

        // Crear usuario para el doctor
        AppUser user1 = AppUser.create("doctest@test.com", "hashedpassword", Role.MEDICO);
        em.persist(user1);

        // Crear doctor
        doctor1 = new Doctor();
        doctor1.setUser(user1);
        doctor1.setFirstName("Doctor");
        doctor1.setLastName("Test");
        doctor1.setLicenseNumber("LICTEST999");
        em.persist(doctor1);

        // Crear paciente
        patient = new Patient();
        patient.setFirstName("Paciente");
        patient.setLastName("RepoTest");
        patient.setDni("REPOTEST001");
        em.persist(patient);

        em.flush();
    }

    // D1. Buscar citas por doctorId devuelve solo las suyas
    @Test
    @DisplayName("D1: findByDoctorId devuelve solo citas del doctor indicado")
    void findByDoctorId_returnsOnlyDoctorAppointments() {
        Appointment appt1 = new Appointment();
        appt1.setDoctor(doctor1);
        appt1.setPatient(patient);
        appt1.setStartAt(baseTime);
        appt1.setEndAt(baseTime.plusMinutes(30));
        appt1.setStatus(AppointmentStatus.PROGRAMADA);
        em.persist(appt1);
        em.flush();

        List<Appointment> result = appts.findByDoctorId(doctor1.getId());

        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(a -> a.getDoctor().getId().equals(doctor1.getId())));
    }

    // D2. Buscar citas por rango de fechas
    @Test
    @DisplayName("D2: findByDoctorIdAndStartAtBetween devuelve citas en el rango")
    void findByDoctorIdAndStartAtBetween_returnsAppointmentsInRange() {
        Appointment apptInRange = new Appointment();
        apptInRange.setDoctor(doctor1);
        apptInRange.setPatient(patient);
        apptInRange.setStartAt(baseTime);
        apptInRange.setEndAt(baseTime.plusMinutes(30));
        apptInRange.setStatus(AppointmentStatus.PROGRAMADA);
        em.persist(apptInRange);
        em.flush();

        LocalDateTime from = baseTime.minusHours(1);
        LocalDateTime to = baseTime.plusHours(1);

        List<Appointment> result = appts.findByDoctorIdAndStartAtBetween(doctor1.getId(), from, to);

        assertEquals(1, result.size());
    }

    // D3. Buscar por doctorId + estado
    @Test
    @DisplayName("D3: findByDoctorIdAndStatus devuelve solo citas con el estado indicado")
    void findByDoctorIdAndStatus_returnsFilteredAppointments() {
        Appointment apptProgramada = new Appointment();
        apptProgramada.setDoctor(doctor1);
        apptProgramada.setPatient(patient);
        apptProgramada.setStartAt(baseTime);
        apptProgramada.setEndAt(baseTime.plusMinutes(30));
        apptProgramada.setStatus(AppointmentStatus.PROGRAMADA);
        em.persist(apptProgramada);

        Appointment apptCompletada = new Appointment();
        apptCompletada.setDoctor(doctor1);
        apptCompletada.setPatient(patient);
        apptCompletada.setStartAt(baseTime.plusHours(2));
        apptCompletada.setEndAt(baseTime.plusHours(2).plusMinutes(30));
        apptCompletada.setStatus(AppointmentStatus.COMPLETADA);
        em.persist(apptCompletada);
        em.flush();

        List<Appointment> result = appts.findByDoctorIdAndStatus(doctor1.getId(), AppointmentStatus.PROGRAMADA);

        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(a -> a.getStatus() == AppointmentStatus.PROGRAMADA));
    }

    // D4. Restricción unique provoca excepción
    @Test
    @DisplayName("D4: Guardar paciente con DNI duplicado lanza excepción")
    void saveDuplicateDni_throwsException() {
        Patient patient2 = new Patient();
        patient2.setFirstName("Otro");
        patient2.setLastName("Paciente");
        patient2.setDni("REPOTEST001"); // Mismo DNI

        assertThrows(Exception.class, () -> {
            patientRepository.saveAndFlush(patient2);
        });
    }
}