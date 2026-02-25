package com.azenticsys.polaris.systemvalue.controller;

import com.azenticsys.polaris.common.pagination.PageQuery;
import com.azenticsys.polaris.common.pagination.PageResponse;
import com.azenticsys.polaris.systemvalue.dto.CreateSystemValueRequest;
import com.azenticsys.polaris.systemvalue.dto.SystemValueFilter;
import com.azenticsys.polaris.systemvalue.dto.SystemValueResponse;
import com.azenticsys.polaris.systemvalue.dto.UpdateSystemValueRequest;
import com.azenticsys.polaris.systemvalue.service.SystemValueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/systemvalues")
@RequiredArgsConstructor
public class SystemValueController {

    private final SystemValueService systemValueService;

    @PostMapping
    public ResponseEntity<SystemValueResponse> create(
            @Valid @RequestBody CreateSystemValueRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(systemValueService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SystemValueResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(systemValueService.findById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<SystemValueResponse>> findAll(
            @RequestParam(defaultValue = "0")            int page,
            @RequestParam(defaultValue = "10")           int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc")          String sortDir,
            @RequestParam(required = false)              String catalogType,
            @RequestParam(required = false)              String value,
            @RequestParam(required = false)              String label,
            @RequestParam(required = false)              Boolean isActive
    ) {
        PageQuery pageQuery = new PageQuery(page, size, sortBy, sortDir);
        SystemValueFilter filter = new SystemValueFilter(catalogType, value, label, isActive);
        return ResponseEntity.ok(systemValueService.findAll(pageQuery, filter));
    }

    @GetMapping("/catalog/{catalogType}")
    public ResponseEntity<List<SystemValueResponse>> findByCatalogType(
            @PathVariable String catalogType
    ) {
        return ResponseEntity.ok(systemValueService.findByCatalogType(catalogType));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SystemValueResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSystemValueRequest request
    ) {
        return ResponseEntity.ok(systemValueService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        systemValueService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
