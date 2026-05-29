package com.innowise.userservice.repository;

import com.innowise.userservice.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {
    @Query(value = """
            SELECT * FROM payment_cards
            WHERE id = :id
            """, nativeQuery = true)
    Optional<PaymentCard> findPaymentCardById(@Param("id") Long id);

    @Query("SELECT pc FROM PaymentCard pc WHERE pc.user.id = :userId")
    List<PaymentCard> findAllByUserId(@Param("userId") UUID userId);

    boolean existsByNumber(String number);

    long countPaymentCardByUserId(UUID userId);
}
