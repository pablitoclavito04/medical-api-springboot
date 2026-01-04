package com.example.medical.service;

import com.example.medical.dto.DoctorResponse;
import com.example.medical.error.NotFoundException;
import com.example.medical.repo.DoctorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorServiceImpl(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Override
    public Page<DoctorResponse> list(Pageable pageable) {
        return doctorRepository.findAll(pageable)
                .map(d -> new DoctorResponse(
                        d.getId(),
                        d.getFirstName(),
                        d.getLastName(),
                        d.getLicenseNumber()
                ));
    }

    @Override
    public DoctorResponse getById(Long id) {
        return doctorRepository.findById(id)
                .map(d -> new DoctorResponse(
                        d.getId(),
                        d.getFirstName(),
                        d.getLastName(),
                        d.getLicenseNumber()
                ))
                .orElseThrow(() -> new NotFoundException("Doctor not found"));
    }
}