package com.exemple.reservationHotel.repository;

import com.exemple.reservationHotel.entity.Paiement;
import com.exemple.reservationHotel.entity.Reservation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    // Trouver un paiement par réservation
    Paiement findByReservation(Reservation reservation);

    boolean existsByReservation(Reservation reservation);

    List<Paiement> findByReservationId(Long reservationId);
}
