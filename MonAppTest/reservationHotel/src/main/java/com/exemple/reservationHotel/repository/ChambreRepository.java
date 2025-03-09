package com.exemple.reservationHotel.repository;

import com.exemple.reservationHotel.entity.Chambre;
import com.exemple.reservationHotel.entity.TypeChambre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChambreRepository extends JpaRepository<Chambre, Long> {

    Chambre findByTypeChambre(TypeChambre typeChambre);  // Recherche une chambre du type spécifié
}
