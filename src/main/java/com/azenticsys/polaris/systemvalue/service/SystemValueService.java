package com.azenticsys.polaris.systemvalue.service;

import com.azenticsys.polaris.common.pagination.PageQuery;
import com.azenticsys.polaris.common.pagination.PageResponse;
import com.azenticsys.polaris.systemvalue.dto.CreateSystemValueRequest;
import com.azenticsys.polaris.systemvalue.dto.SystemValueFilter;
import com.azenticsys.polaris.systemvalue.dto.SystemValueResponse;
import com.azenticsys.polaris.systemvalue.dto.UpdateSystemValueRequest;

import java.util.List;
import java.util.UUID;

public interface SystemValueService {

    SystemValueResponse create(CreateSystemValueRequest request);

    SystemValueResponse findById(UUID id);

    PageResponse<SystemValueResponse> findAll(PageQuery pageQuery, SystemValueFilter filter);

    List<SystemValueResponse> findByCatalogType(String catalogType);

    SystemValueResponse update(UUID id, UpdateSystemValueRequest request);

    void softDelete(UUID id);
}
