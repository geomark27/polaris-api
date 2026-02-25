package com.azenticsys.polaris.systemvalue.repository;

import com.azenticsys.polaris.systemvalue.entity.SystemValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SystemValueRepository extends JpaRepository<SystemValue, UUID>, JpaSpecificationExecutor<SystemValue> {

    boolean existsByCatalogTypeAndValue(String catalogType, String value);

    boolean existsByCatalogTypeAndValueAndIdNot(String catalogType, String value, UUID id);

    List<SystemValue> findByCatalogTypeAndDeletedAtIsNullOrderByDisplayOrderAsc(String catalogType);
}
