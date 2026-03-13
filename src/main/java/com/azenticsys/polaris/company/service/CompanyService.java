package com.azenticsys.polaris.company.service;

import com.azenticsys.polaris.company.dto.CreateCompanyRequest;
import com.azenticsys.polaris.company.dto.UpdateCompanyRequest;
import com.azenticsys.polaris.company.dto.CompanyResponse;

import java.util.List;
import java.util.UUID;

public interface CompanyService {

    CompanyResponse create(CreateCompanyRequest request);

    CompanyResponse findById(UUID id);

    List<CompanyResponse> findAll();

    CompanyResponse update(UUID id, UpdateCompanyRequest request);

    void softDelete(UUID id);
}
