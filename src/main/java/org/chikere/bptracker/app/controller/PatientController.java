package org.chikere.bptracker.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.chikere.bptracker.app.model.Patient;
import org.chikere.bptracker.app.service.PatientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Controller for handling patient-related requests
 */
@Controller
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    /**
     * Display the patient list page
     * @param page the page number
     * @param size the page size
     * @param model the model
     * @return the patient list page view
     */
    @GetMapping
    public String listPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Page<Patient> patientPage = patientService.getAllPatients(
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "lastName", "firstName")));

        model.addAttribute("patients", patientPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", patientPage.getTotalPages());

        return "patients/list";
    }

    /**
     * Search for patients by name
     * @param searchTerm the search term
     * @param model the model
     * @return the patient search results page view
     */
    @GetMapping("/search")
    public String searchPatients(@RequestParam String searchTerm, Model model) {
        List<Patient> patients = patientService.searchPatientsByName(searchTerm);
        model.addAttribute("patients", patients);
        model.addAttribute("searchTerm", searchTerm);
        return "patients/search-results";
    }

    /**
     * Display the patient registration page
     * @param model the model
     * @return the patient registration page view
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("patient", new Patient());
        model.addAttribute("genders", Patient.Gender.values());
        return "patients/register";
    }

    /**
     * Process the patient registration form
     * @param patient the patient to register
     * @param bindingResult the binding result
     * @param redirectAttributes the redirect attributes
     * @return the redirect URL
     */
    @PostMapping("/register")
    public String registerPatient(@Valid @ModelAttribute("patient") Patient patient,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "patients/register";
        }

        try {
            Patient savedPatient = patientService.createPatient(patient);
            redirectAttributes.addFlashAttribute("successMessage", "Patient registered successfully!");
            return "redirect:/patients/" + savedPatient.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error registering patient: " + e.getMessage());
            return "redirect:/patients/register";
        }
    }

    /**
     * Display the patient details page
     * @param id the patient ID
     * @param model the model
     * @return the patient details page view
     */
    @GetMapping("/{id}")
    public String viewPatient(@PathVariable Long id, Model model) {
        Optional<Patient> patientOpt = patientService.getPatientById(id);

        if (patientOpt.isPresent()) {
            model.addAttribute("patient", patientOpt.get());
            return "patients/view";
        } else {
            model.addAttribute("errorMessage", "Patient not found with ID: " + id);
            return "error/not-found";
        }
    }

    /**
     * Display the patient edit page
     * @param id the patient ID
     * @param model the model
     * @return the patient edit page view
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Patient> patientOpt = patientService.getPatientById(id);

        if (patientOpt.isPresent()) {
            model.addAttribute("patient", patientOpt.get());
            model.addAttribute("genders", Patient.Gender.values());
            return "patients/edit";
        } else {
            model.addAttribute("errorMessage", "Patient not found with ID: " + id);
            return "error/not-found";
        }
    }

    /**
     * Process the patient edit form
     * @param id the patient ID
     * @param patient the updated patient
     * @param bindingResult the binding result
     * @param redirectAttributes the redirect attributes
     * @return the redirect URL
     */
    @PostMapping("/{id}/edit")
    public String updatePatient(@PathVariable Long id,
                               @Valid @ModelAttribute("patient") Patient patient,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "patients/edit";
        }

        try {
            patientService.updatePatient(id, patient);
            redirectAttributes.addFlashAttribute("successMessage", "Patient updated successfully!");
            return "redirect:/patients/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating patient: " + e.getMessage());
            return "redirect:/patients/" + id + "/edit";
        }
    }

    /**
     * Delete a patient
     * @param id the patient ID
     * @param redirectAttributes the redirect attributes
     * @return the redirect URL
     */
    @PostMapping("/{id}/delete")
    public String deletePatient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            patientService.deletePatient(id);
            redirectAttributes.addFlashAttribute("successMessage", "Patient deleted successfully!");
            return "redirect:/patients";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting patient: " + e.getMessage());
            return "redirect:/patients/" + id;
        }
    }

    /**
     * Display the dashboard page
     * @param model the model
     * @return the dashboard page view
     */
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // Get the 5 most recently registered patients
        Page<Patient> recentPatients = patientService.getAllPatients(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "registrationDate")));

        model.addAttribute("recentPatients", recentPatients.getContent());
        model.addAttribute("totalPatients", patientService.getAllPatients().size());

        return "dashboard";
    }
}
