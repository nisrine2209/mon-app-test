package com.exemple.reservationHotel.repository;

import com.exemple.reservationHotel.entity.Client;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Client findByEmail(String email); // Méthode personnalisée

    boolean existsByEmail(String email); // Ajout de la méthode existsByEmail

}
