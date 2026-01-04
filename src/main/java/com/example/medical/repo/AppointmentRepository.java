package com.example.medical.repo;

import com.example.medical.domain.Appointment;
import com.example.medical.domain.AppointmentStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

    List<Appointment> findByDoctorIdAndStartAtBetween(Long doctorId, LocalDateTime from, LocalDateTime to);

    @Query("""
        SELECT a FROM Appointment a
        WHERE (:doctorId IS NULL OR a.doctor.id = :doctorId)
        AND (:patientId IS NULL OR a.patient.id = :patientId)
        AND (:status IS NULL OR a.status = :status)
        AND (:dateFrom IS NULL OR a.startAt >= :dateFrom)
        AND (:dateTo IS NULL OR a.startAt <= :dateTo)
    """)
    Page<Appointment> findWithFilters(
            @Param("doctorId") Long doctorId,
            @Param("patientId") Long patientId,
            @Param("status") AppointmentStatus status,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable
    );

    @Query("""
        SELECT COUNT(a) > 0 FROM Appointment a
        WHERE a.doctor.id = :doctorId
        AND a.status = 'PROGRAMADA'
        AND a.id <> :excludeId
        AND (
            (a.startAt < :endAt AND a.endAt > :startAt)
        )
    """)
    boolean existsOverlapping(
            @Param("doctorId") Long doctorId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("excludeId") Long excludeId
    );
}