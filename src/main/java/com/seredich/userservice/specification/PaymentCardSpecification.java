package com.seredich.userservice.specification;

import com.seredich.userservice.dto.SearchPaymentCardDto;
import com.seredich.userservice.dto.SearchUserDto;
import com.seredich.userservice.entity.PaymentCard;
import com.seredich.userservice.entity.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PaymentCardSpecification {

    public static Specification<PaymentCard> build(SearchPaymentCardDto searchPaymentCardDto) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            addHolderPredicate(
                    searchPaymentCardDto.holder(),
                    root,
                    criteriaBuilder,
                    predicates
            );

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addHolderPredicate(
            String name,
            Root<PaymentCard> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates) {

        if (name != null && !name.isBlank()) {
            String cleanHolder = name.trim().toLowerCase();
            Predicate predicate = criteriaBuilder.like(
                    criteriaBuilder.lower(
                            root.get("name")), "%" + cleanHolder + "%");
            predicates.add(predicate);
        }
    }
}
