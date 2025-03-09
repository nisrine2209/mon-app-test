package com.exemple.reservationHotel.controller;

import com.exemple.reservationHotel.entity.Chambre;
import com.exemple.reservationHotel.entity.Client;
import com.exemple.reservationHotel.entity.MoyenPaiement;
import com.exemple.reservationHotel.entity.Paiement;
import com.exemple.reservationHotel.entity.Reservation;
import com.exemple.reservationHotel.repository.ClientRepository;
import com.exemple.reservationHotel.repository.ReservationRepository;
import com.exemple.reservationHotel.repository.ChambreRepository;
import com.exemple.reservationHotel.repository.PaiementRepository;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ChambreRepository chambreRepository;

    @Autowired
    private PaiementRepository paiementRepository;

    // Afficher la page de création d'une nouvelle réservation
    @GetMapping("/showNewReservationForm")
    public String showNewReservationForm(Model model) {
        Reservation reservation = new Reservation();

        // Récupérer les clients
        List<Client> clients = clientRepository.findAll();

        // Récupérer toutes les chambres
        List<Chambre> chambres = chambreRepository.findAll();

        // Filtrer les chambres disponibles (en fonction des dates)
        List<Chambre> chambresDisponibles = new ArrayList<>();
        for (Chambre chambre : chambres) {
            if (checkChambreAvailability(chambre.getId(), reservation.getDateDebut(), reservation.getDateFin())) {
                chambresDisponibles.add(chambre);
            }
        }

        // Ajouter les données au modèle
        model.addAttribute("reservation", reservation);
        model.addAttribute("clients", clients);
        model.addAttribute("chambres", chambresDisponibles);

        return "new_reservation"; // La vue du formulaire
    }

    @GetMapping("/list")
    public String listReservations(Model model) {
        List<Reservation> reservations = reservationRepository.findAll();
        model.addAttribute("reservations", reservations);
        return "reservations"; // La vue où la liste des réservations sera affichée
    }

    // Ajout d'une réservation via un formulaire
    @PostMapping("/saveNewReservation")
    public String saveNewReservation(
            @RequestParam("client") Long clientId,
            @RequestParam("chambreId") Long chambreId,
            @RequestParam("dateDebut") String dateDebut,
            @RequestParam("dateFin") String dateFin,
            Model model) {

        // Récupérer les clients et les chambres pour le formulaire
        List<Client> clients = clientRepository.findAll();
        List<Chambre> chambres = chambreRepository.findAll();
        model.addAttribute("clients", clients);
        model.addAttribute("chambres", chambres);

        // Convertir les dates
        LocalDate dateDebutLocal = LocalDate.parse(dateDebut);
        LocalDate dateFinLocal = LocalDate.parse(dateFin);
        long diffDays = ChronoUnit.DAYS.between(dateDebutLocal, dateFinLocal);

        // Vérification des dates
        if (diffDays <= 0) {
            model.addAttribute("error", "La date de fin doit être après la date de début.");
            return "new_reservation"; // Retourner le formulaire avec le message d'erreur
        }

        // Vérifier si le client et la chambre existent
        Client client = clientRepository.findById(clientId).orElseThrow();
        Chambre chambre = chambreRepository.findById(chambreId).orElseThrow();

        // Vérifier la disponibilité de la chambre
        boolean isAvailable = checkChambreAvailability(chambreId, dateDebutLocal, dateFinLocal);
        if (!isAvailable) {
            model.addAttribute("error", "La chambre sélectionnée n'est pas disponible pour les dates choisies.");
            model.addAttribute("reservation", new Reservation(client, chambre, dateDebutLocal, dateFinLocal, null)); // Conserver les données saisies
            return "new_reservation"; // Retourner le formulaire avec le message d'erreur
        }

        // Calculer le prix total
        double prixTotal = chambre.getTarifParNuit() * diffDays;

        // Créer et sauvegarder la réservation
        Reservation reservation = new Reservation(client, chambre, dateDebutLocal, dateFinLocal, prixTotal);
        reservationRepository.save(reservation);

        // Rediriger après une réservation réussie
        return "redirect:/reservations/list"; // Redirige vers la liste des réservations
    }

    @GetMapping("/deleteReservation")
    public String deleteReservation(@RequestParam("id") Long id) {
        reservationRepository.deleteById(id);
        return "redirect:/reservations/list";
    }

    private boolean checkChambreAvailability(Long chambreId, LocalDate dateDebut, LocalDate dateFin) {
        List<Reservation> existingReservations = reservationRepository.findByChambreIdAndDates(chambreId, dateDebut, dateFin);
        return existingReservations.isEmpty();
    }

    @GetMapping("/paiements")
    public String showPaymentPage() {
        return "paiements"; // Assure-toi que ce chemin correspond à une vue qui existe.
    }

    @GetMapping("/paiements/{id}")
    public String showPaymentPage(@PathVariable("id") Long reservationId, Model model) {
        // Récupérer la réservation en fonction de l'ID
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();

        // Vérifier si la réservation a déjà été payée
        boolean paiementEffectue = reservation.getPaiements().stream().anyMatch(p -> p.getReservation().getId().equals(reservationId));

        if (paiementEffectue) {
            // Si un paiement existe déjà, afficher une confirmation de paiement
            model.addAttribute("paymentSuccess", true);
            model.addAttribute("reservation", reservation);
            return "confirmation_paiement"; // Page de confirmation du paiement
        } else {
            // Sinon, afficher la page de paiement
            model.addAttribute("paymentSuccess", false);
            model.addAttribute("reservation", reservation);
            return "paiements"; // Page de paiement
        }
    }

    @PostMapping("/paiements/{id}")
    public String processPayment(@PathVariable("id") Long reservationId,
            @RequestParam("paymentMethod") String paymentMethod,
            Model model) {
        // Trouver la réservation par son ID
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        // Vérifier si un paiement a déjà été effectué pour cette réservation
        boolean paiementEffectue = reservation.getPaiements().stream().anyMatch(p -> p.getReservation().getId().equals(reservationId));

        if (paiementEffectue) {
            // Si un paiement existe déjà, rediriger vers la page de confirmation
            model.addAttribute("paymentSuccess", true);
            model.addAttribute("reservation", reservation);
            return "confirmation_paiement"; // Page de confirmation du paiement
        }

        // Convertir la méthode de paiement en énumération
        MoyenPaiement moyenPaiement = MoyenPaiement.valueOf(paymentMethod.toUpperCase());

        // Créer un nouvel objet Paiement
        Paiement paiement = new Paiement();
        paiement.setReservation(reservation);  // Associer le paiement à la réservation
        paiement.setMontant(reservation.getPrixTotal());  // Utiliser le prix de la réservation
        paiement.setMoyenPaiement(moyenPaiement); // Définir le moyen de paiement

        // Enregistrer le paiement dans la base de données
        paiementRepository.save(paiement);  // Sauvegarder le paiement dans la table 'paiements'

        // Ajouter des attributs au modèle pour afficher la confirmation
        model.addAttribute("paymentSuccess", true);
        model.addAttribute("paymentMethod", moyenPaiement);
        model.addAttribute("reservation", reservation);

        // Retourner la vue de paiement (confirmation)
        return "confirmation_paiement"; // Page de confirmation du paiement
    }

    @GetMapping("/filterByDate")
    public String filterReservationsByDate(@RequestParam(value = "dateDebut", required = false) String dateDebutStr, Model model) {
        // Vérifier si la date est vide ou non fournie
        if (dateDebutStr == null || dateDebutStr.isEmpty()) {
            // Ajouter un message d'erreur avec le nom "errorMessage"
            model.addAttribute("errorMessage", "Veuillez sélectionner une date.");
            return "reservations"; // Rediriger vers la vue sans filtrer
        }

        // Convertir la chaîne de caractères de la date de début en un objet LocalDate
        LocalDate dateDebut = LocalDate.parse(dateDebutStr);

        // Récupérer les réservations dont la date de début est après la date sélectionnée
        List<Reservation> filteredReservations = reservationRepository.findByDateDebutAfter(dateDebut);

        // Ajouter les réservations filtrées au modèle
        model.addAttribute("reservations", filteredReservations);

        return "reservations"; // La vue où les réservations filtrées seront affichées
    }
}
