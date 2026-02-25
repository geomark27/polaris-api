package com.azenticsys.polaris.systemvalue.service;

import com.azenticsys.polaris.common.pagination.PageQuery;
import com.azenticsys.polaris.common.pagination.PageResponse;
import com.azenticsys.polaris.systemvalue.dto.CreateSystemValueRequest;
import com.azenticsys.polaris.systemvalue.dto.SystemValueFilter;
import com.azenticsys.polaris.systemvalue.dto.SystemValueResponse;
import com.azenticsys.polaris.systemvalue.dto.UpdateSystemValueRequest;
import com.azenticsys.polaris.systemvalue.entity.SystemValue;
import com.azenticsys.polaris.systemvalue.repository.SystemValueRepository;
import com.azenticsys.polaris.systemvalue.repository.SystemValueSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SystemValueServiceImpl implements SystemValueService {

    private final SystemValueRepository systemValueRepository;

    @Override
    @Transactional
    public SystemValueResponse create(CreateSystemValueRequest request) {
        if (systemValueRepository.existsByCatalogTypeAndValue(request.catalogType(), request.value())) {
            throw new IllegalArgumentException(
                    "Value '" + request.value() + "' already exists in catalog '" + request.catalogType() + "'"
            );
        }

        SystemValue entity = SystemValue.builder()
                .catalogType(request.catalogType().toUpperCase())
                .value(request.value().toUpperCase())
                .label(request.label())
                .description(request.description())
                .displayOrder(request.displayOrder())
                .build();

        return SystemValueResponse.from(systemValueRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public SystemValueResponse findById(UUID id) {
        return SystemValueResponse.from(getActive(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SystemValueResponse> findAll(PageQuery pageQuery, SystemValueFilter filter) {
        return PageResponse.from(
                systemValueRepository.findAll(
                        SystemValueSpecification.withFilter(filter),
                        pageQuery.toPageable()
                ).map(SystemValueResponse::from)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemValueResponse> findByCatalogType(String catalogType) {
        return systemValueRepository
                .findByCatalogTypeAndDeletedAtIsNullOrderByDisplayOrderAsc(catalogType.toUpperCase())
                .stream()
                .map(SystemValueResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public SystemValueResponse update(UUID id, UpdateSystemValueRequest request) {
        SystemValue entity = getActive(id);

        if (request.label() != null && !request.label().isBlank()) {
            entity.setLabel(request.label());
        }
        if (request.description() != null) {
            entity.setDescription(request.description());
        }
        if (request.displayOrder() != null) {
            entity.setDisplayOrder(request.displayOrder());
        }

        return SystemValueResponse.from(systemValueRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        SystemValue entity = getActive(id);
        entity.setDeletedAt(LocalDateTime.now());
        entity.setActive(false);
        systemValueRepository.save(entity);
    }

    private SystemValue getActive(UUID id) {
        return systemValueRepository.findById(id)
                .filter(e -> e.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("SystemValue not found with id: " + id));
    }
}
