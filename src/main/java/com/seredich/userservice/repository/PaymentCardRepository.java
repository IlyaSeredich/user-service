package com.seredich.userservice.repository;

import com.seredich.userservice.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {
    PaymentCard save(PaymentCard paymentCard);

    @Query(value = """
            SELECT * FROM payment_cards
            WHERE id = :id
            """, nativeQuery = true)
    PaymentCard getPaymentCardById(@Param("id") Long id);

    @Query("SELECT pc FROM PaymentCard pc WHERE pc.user.id = :userId")
    List<PaymentCard> getAllByUserId(@Param("userId") Long userId);
}
