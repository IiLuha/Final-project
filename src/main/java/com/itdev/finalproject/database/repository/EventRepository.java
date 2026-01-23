package com.itdev.finalproject.database.repository;

import com.itdev.finalproject.database.entity.EventEntity;
import com.itdev.finalproject.database.entity.EventStatus;
import com.itdev.finalproject.database.entity.LocationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface EventRepository extends JpaRepository<EventEntity, Long>, JpaSpecificationExecutor<EventEntity> {

    Page<EventEntity> findAllByOwnerId(Long ownerId, Pageable pageable);

    @Query("SELECT e FROM UserEntity u " +
            "JOIN u.attendedEvents e " +
            "WHERE u.id = :visitorId")
    Page<EventEntity> findAllByVisitorId(@Param("visitorId") Long visitorId, Pageable pageable);

    boolean existsByLocation(LocationEntity location);

    List<EventEntity> findAllByDateBeforeAndStatusIs(LocalDateTime dateBefore, EventStatus status);

    @Modifying
    @Query(value =
            "UPDATE event " +
            "SET status = :status " +
            "WHERE id = ANY(:ids)",
            nativeQuery = true
    )
    void updateStatusesByIds(@Param("status") String status,
                             @Param("ids") Long[] ids);

    List<EventEntity> findAllByDateBeforeAndStatusIn(LocalDateTime dateBefore, Collection<EventStatus> statuses);
}
