package com.itdev.finalproject.database.predicate;

import org.springframework.data.jpa.domain.Specification;

public interface JpaSpecificationCreator<F, E> {

    Specification<E> createSpecification(F filter);
}
