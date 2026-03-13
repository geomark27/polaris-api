package com.azenticsys.polaris.company.controller;

import com.azenticsys.polaris.company.dto.CreateCompanyRequest;
import com.azenticsys.polaris.company.dto.UpdateCompanyRequest;
import com.azenticsys.polaris.company.dto.CompanyResponse;
import com.azenticsys.polaris.company.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companys")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<CompanyResponse> create(@Valid @RequestBody CreateCompanyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(companyService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(companyService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<CompanyResponse>> findAll() {
        return ResponseEntity.ok(companyService.findAll());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CompanyResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCompanyRequest request
    ) {
        return ResponseEntity.ok(companyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        companyService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
