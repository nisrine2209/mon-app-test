package com.exemple.reservationHotel.controller;

import com.exemple.reservationHotel.entity.Client;
import com.exemple.reservationHotel.repository.ClientRepository;
import com.exemple.reservationHotel.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clients")
public class ClientController {

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    // Afficher la page d'accueil avec la liste des clients
    @GetMapping("/")
    public String viewHomePage(Model model) {
        List<Client> listClients = clientRepository.findAll();
        model.addAttribute("listClients", listClients);
        return "index"; // Correspond à index.html

    }

    @GetMapping("/list")
    public String getClients(Model model) {
        List<Client> clients = clientRepository.findAll();
        model.addAttribute("listClients", clients);
        return "clients"; // Cela redirige vers clients.html dans /templates/
    }

    // Afficher le formulaire de création de client
    @GetMapping("/showNewClientForm")
    public String showNewClientForm(Model model) {
        Client client = new Client(); // Créer un objet vide pour le formulaire
        model.addAttribute("client", client);
        return "new_client"; // Retourne la vue pour ajouter un client
    }

    @PostMapping("/saveNewClient")
    public String saveNewClient(@ModelAttribute("client") @Valid Client client, Errors errors, Model model) {
        // Vérifier si l'email existe déjà dans la base de données
        if (clientRepository.existsByEmail(client.getEmail())) {
            // Ajouter un message d'erreur dans le modèle
            model.addAttribute("errorMessage", "Cet email est déjà utilisé.");
            return "new_client"; // Retourner à la page avec le message d'erreur
        }

        // Vérifier si des erreurs existent dans le formulaire
        if (errors.hasErrors()) {
            return "new_client"; // Si des erreurs, retourner la vue
        }

        // Sauvegarder le client dans la base de données
        clientRepository.save(client);
        return "redirect:/"; // Redirection vers la page d'accueil
    }

    // Afficher un client par email
    @GetMapping("/{email}")
    public String trouverClientParEmail(@PathVariable String email, Model model) {
        Client client = clientRepository.findByEmail(email); // Trouver le client par email
        if (client != null) {
            model.addAttribute("client", client); // Ajouter le client à la vue
            return "client_details"; // Retourne la vue avec les détails du client
        } else {
            return "client_not_found"; // Retourne une vue si le client n'est pas trouvé
        }
    }

    @GetMapping("/search")
    public String chercherClientParEmail(@RequestParam String email, Model model) {
        Client client = clientRepository.findByEmail(email);
        if (client != null) {
            model.addAttribute("listClients", List.of(client));
            return "clients";
        } else {
            model.addAttribute("errorMessage", "Aucun client trouvé avec l'email : " + email);
            return "clients";
        }
    }

    @GetMapping("/deleteClient")
    public String deleteClient(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        if (reservationRepository.existsByClientId(id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Impossible de supprimer ce client car il a des réservations associées.");
            return "redirect:/clients/list";
        }
        clientRepository.deleteById(id);
        return "redirect:/clients/list";
    }

    @GetMapping("/editClient")
    public String showEditClientForm(@RequestParam("id") Long id, Model model) {
        // Récupérer le client par son ID
        Client client = clientRepository.findById(id).orElseThrow(() -> new RuntimeException("Client non trouvé"));

        // Ajouter le client au modèle
        model.addAttribute("client", client);
        return "edit_client"; // Retourner la vue pour modifier un client
    }

    @PostMapping("/updateClient")
    public String updateClient(@ModelAttribute("client") @Valid Client client, Errors errors, Model model) {
        // Vérifier si des erreurs existent dans le formulaire
        if (errors.hasErrors()) {
            return "edit_client"; // Si des erreurs, retourner la vue
        }

        // Sauvegarder les modifications du client dans la base de données
        clientRepository.save(client);
        return "redirect:/clients/list"; // Rediriger vers la liste des clients après la mise à jour
    }

}
