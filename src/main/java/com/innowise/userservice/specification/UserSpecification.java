package com.innowise.userservice.specification;

import com.innowise.userservice.dto.SearchUserDto;
import com.innowise.userservice.entity.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> build(SearchUserDto searchUserDto) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            addNamePredicate(
                    searchUserDto.name(),
                    root,
                    criteriaBuilder,
                    predicates
            );

            addSurnamePredicate(
                    searchUserDto.surname(),
                    root,
                    criteriaBuilder,
                    predicates
            );

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addNamePredicate(
            String name,
            Root<User> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates) {

        if (name != null && !name.isBlank()) {
            String cleanName = name.trim().toLowerCase();
            Predicate predicate = criteriaBuilder.like(
                    criteriaBuilder.lower(
                            root.get("name")), "%" + cleanName + "%");
            predicates.add(predicate);
        }
    }

    private static void addSurnamePredicate(
            String surname,
            Root<User> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates) {

        if(surname != null && !surname.isBlank()) {
            String cleanSurname = surname.trim().toLowerCase();
            Predicate predicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("surname")),
                    "%" + cleanSurname + "%"
            );
            predicates.add(predicate);
        }
    }
}
