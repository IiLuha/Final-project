package com.itdev.finalproject.database.predicate;

import com.itdev.finalproject.database.entity.EventEntity;
import com.itdev.finalproject.database.entity.EventEntity_;
import com.itdev.finalproject.database.entity.EventStatus;
import com.itdev.finalproject.database.entity.LocationEntity_;
import com.itdev.finalproject.dto.filter.EventFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EventJpaPredicateBuilder implements JpaSpecificationCreator<EventFilter, EventEntity> {


    @Override
    public Specification<EventEntity> createSpecification(EventFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.name() != null) {
                predicates.add(cb.like(root.get(EventEntity_.NAME), "%"+filter.name()+"%"));
            }
            if (filter.maxPlacesMin() != null) {
                predicates.add(cb.ge(root.get(EventEntity_.MAX_PLACES), filter.maxPlacesMin()));
            }
            if (filter.maxPlacesMax() != null) {
                predicates.add(cb.le(root.get(EventEntity_.MAX_PLACES), filter.maxPlacesMax()));
            }
            if (filter.after() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(EventEntity_.DATE), filter.after()));
            }
            if (filter.before() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get(EventEntity_.DATE), filter.before()));
            }
            if (filter.costMin() != null) {
                predicates.add(cb.ge(root.get(EventEntity_.COST), filter.costMin()));
            }
            if (filter.costMax() != null) {
                predicates.add(cb.le(root.get(EventEntity_.COST), filter.costMax()));
            }
            if (filter.durationMin() != null) {
                predicates.add(cb.ge(root.get(EventEntity_.DURATION), filter.durationMin()));
            }
            if (filter.durationMax() != null) {
                predicates.add(cb.le(root.get(EventEntity_.DURATION), filter.durationMax()));
            }
            if (filter.locationId() != null) {
                var location = root.join(EventEntity_.LOCATION);
                predicates.add(cb.equal(location.get(LocationEntity_.ID), filter.locationId()));
            }
            var eventStatuses = filter.eventStatuses();
            if (eventStatuses != null && eventStatuses.length > 0) {
                List<Predicate> predicateStatuses = new ArrayList<>(eventStatuses.length);
                for (EventStatus status : eventStatuses) {
                    predicateStatuses.add(cb.equal(root.get(EventEntity_.STATUS), status));
                }
                predicates.add(cb.or(predicateStatuses));
            }
            return cb.and(predicates);
        };
    }
}
