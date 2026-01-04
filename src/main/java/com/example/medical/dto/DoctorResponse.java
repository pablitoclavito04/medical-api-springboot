package com.example.medical.dto;

public record DoctorResponse(
        Long id,
        String firstName,
        String lastName,
        String licenseNumber
) {}