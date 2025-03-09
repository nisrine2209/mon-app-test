package com.exemple.reservationHotel.repository;

import com.exemple.reservationHotel.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByClientId(Long clientId);

    boolean existsByClientId(Long clientId);

    List<Reservation> findByDateDebutAfter(LocalDate dateDebut);

    // Requête personnalisée pour vérifier la disponibilité de la chambre
    @Query("SELECT r FROM Reservation r WHERE r.chambre.id = :chambreId "
            + "AND ((r.dateDebut <= :dateFin AND r.dateFin >= :dateDebut))")
    List<Reservation> findByChambreIdAndDates(@Param("chambreId") Long chambreId,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin);

}
