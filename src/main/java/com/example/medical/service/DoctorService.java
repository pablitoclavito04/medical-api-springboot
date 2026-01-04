package com.example.medical.service;

import com.example.medical.dto.DoctorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DoctorService {
    Page<DoctorResponse> list(Pageable pageable);
    DoctorResponse getById(Long id);
}