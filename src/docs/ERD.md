# Diagrama Entidad-Relación (ERD).

## Sistema Médico - API REST:

```mermaid
erDiagram
    APP_USERS {
        Long id PK
        String email UK
        String password_hash
        String role
    }
    
    PATIENTS {
        Long id PK
        String dni UK
        String first_name
        String last_name
        String phone
        Boolean active
        Long user_id FK
    }
    
    DOCTORS {
        Long id PK
        String first_name
        String last_name
        String license_number UK
        Long user_id FK
    }
    
    SPECIALTIES {
        Long id PK
        String name UK
    }
    
    DOCTOR_SPECIALTIES {
        Long doctor_id FK
        Long specialty_id FK
    }
    
    APPOINTMENTS {
        Long id PK
        Long doctor_id FK
        Long patient_id FK
        LocalDateTime start_at
        LocalDateTime end_at
        String status
        String reason
    }
    
    MEDICAL_RECORDS {
        Long id PK
        Long appointment_id FK
        Long patient_id FK
        Long doctor_id FK
        String diagnosis
        String treatment
        String notes
        LocalDateTime created_at
    }

    APP_USERS ||--o| PATIENTS : "tiene"
    APP_USERS ||--o| DOCTORS : "tiene"
    DOCTORS ||--o{ DOCTOR_SPECIALTIES : "tiene"
    SPECIALTIES ||--o{ DOCTOR_SPECIALTIES : "pertenece"
    DOCTORS ||--o{ APPOINTMENTS : "atiende"
    PATIENTS ||--o{ APPOINTMENTS : "solicita"
    APPOINTMENTS ||--o| MEDICAL_RECORDS : "genera"
    PATIENTS ||--o{ MEDICAL_RECORDS : "tiene"
    DOCTORS ||--o{ MEDICAL_RECORDS : "crea"
```

---

## Descripción de Entidades.

### APP_USERS:
Usuarios del sistema con autenticación JWT.
- **Roles**: ADMIN, RECEPCIONISTA, MEDICO, PACIENTE

### PATIENTS:
Pacientes registrados en el sistema.
- Pueden tener un usuario asociado (opcional)
- DNI único obligatorio

### DOCTORS:
Médicos del sistema.
- Requieren usuario con rol MEDICO
- Número de licencia único

### SPECIALTIES:
Especialidades médicas (Cardiología, Pediatría, etc.)

### APPOINTMENTS:
Citas médicas entre doctor y paciente.
- **Estados**: PROGRAMADA, COMPLETADA, CANCELADA
- Validación de solape de horarios por doctor

### MEDICAL_RECORDS:
Historiales médicos generados tras las citas.
- Vinculados a cita, paciente y doctor

---

## Relaciones:

| Relación | Tipo | Descripción |
|----------|------|-------------|
| User → Patient | 1:1 | Un usuario puede ser paciente |
| User → Doctor | 1:1 | Un usuario puede ser doctor |
| Doctor → Specialty | N:M | Doctores tienen múltiples especialidades |
| Doctor → Appointment | 1:N | Doctor atiende múltiples citas |
| Patient → Appointment | 1:N | Paciente tiene múltiples citas |
| Appointment → MedicalRecord | 1:1 | Cada cita puede generar un historial |

---

## Constraints:

- `APP_USERS.email` - UNIQUE
- `PATIENTS.dni` - UNIQUE
- `DOCTORS.license_number` - UNIQUE
- `SPECIALTIES.name` - UNIQUE
- Validación de negocio: No se permiten citas solapadas para el mismo doctor.