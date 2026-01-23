package com.itdev.finalproject.database.repository;

import com.itdev.finalproject.database.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
    boolean existsByName(String name);
}
