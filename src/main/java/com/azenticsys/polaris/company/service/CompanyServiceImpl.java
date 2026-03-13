package com.azenticsys.polaris.company.service;

import com.azenticsys.polaris.company.dto.CreateCompanyRequest;
import com.azenticsys.polaris.company.dto.UpdateCompanyRequest;
import com.azenticsys.polaris.company.dto.CompanyResponse;
import com.azenticsys.polaris.company.entity.Company;
import com.azenticsys.polaris.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public CompanyResponse create(CreateCompanyRequest request) {
        Company entity = Company.builder()
                // TODO: mapear campos del request
                .build();
        return CompanyResponse.from(companyRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse findById(UUID id) {
        return CompanyResponse.from(getActive(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponse> findAll() {
        return companyRepository.findAll().stream()
                .filter(e -> e.getDeletedAt() == null)
                .map(CompanyResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public CompanyResponse update(UUID id, UpdateCompanyRequest request) {
        Company entity = getActive(id);
        // TODO: aplicar cambios del request
        return CompanyResponse.from(companyRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Company entity = getActive(id);
        entity.setDeletedAt(LocalDateTime.now());
        entity.setActive(false);
        companyRepository.save(entity);
    }

    private Company getActive(UUID id) {
        return companyRepository.findById(id)
                .filter(e -> e.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + id));
    }
}
