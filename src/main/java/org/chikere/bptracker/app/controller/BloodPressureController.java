package org.chikere.bptracker.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.chikere.bptracker.app.model.BloodPressureReading;
import org.chikere.bptracker.app.model.Patient;
import org.chikere.bptracker.app.model.User;
import org.chikere.bptracker.app.service.BloodPressureService;
import org.chikere.bptracker.app.service.PatientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Controller for handling blood pressure reading-related requests
 */
@Controller
@RequestMapping("/bp-readings")
@RequiredArgsConstructor
public class BloodPressureController {

    private final BloodPressureService bpService;
    private final PatientService patientService;

    /**
     * Display the blood pressure reading list page for a patient
     * @param patientId the patient ID
     * @param page the page number
     * @param size the page size
     * @param model the model
     * @return the blood pressure reading list page view
     */
    @GetMapping("/patient/{patientId}")
    public String listReadingsForPatient(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Optional<Patient> patientOpt = patientService.getPatientById(patientId);

        if (patientOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Patient not found with ID: " + patientId);
            return "error/not-found";
        }

        Patient patient = patientOpt.get();
        Page<BloodPressureReading> readingsPage = bpService.getReadingsForPatient(
                patientId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "readingTime")));

        model.addAttribute("patient", patient);
        model.addAttribute("readings", readingsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", readingsPage.getTotalPages());

        return "bp-readings/list";
    }

    /**
     * Display the blood pressure reading form for a patient
     * @param patientId the patient ID
     * @param model the model
     * @return the blood pressure reading form view
     */
    @GetMapping("/patient/{patientId}/record")
    public String showRecordForm(@PathVariable Long patientId, Model model) {
        Optional<Patient> patientOpt = patientService.getPatientById(patientId);

        if (patientOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Patient not found with ID: " + patientId);
            return "error/not-found";
        }

        model.addAttribute("patient", patientOpt.get());
        model.addAttribute("reading", new BloodPressureReading());

        return "bp-readings/record";
    }

    /**
     * Process the blood pressure reading form
     * @param patientId the patient ID
     * @param reading the blood pressure reading
     * @param bindingResult the binding result
     * @param redirectAttributes the redirect attributes
     * @return the redirect URL
     */
    @PostMapping("/patient/{patientId}/record")
    public String recordReading(
            @PathVariable Long patientId,
            @Valid @ModelAttribute("reading") BloodPressureReading reading,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "bp-readings/record";
        }

        try {
            // Get the current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();

            // Save the reading
            BloodPressureReading savedReading = bpService.createReading(patientId, reading, currentUser);

            // Check if the reading is critical
            if (savedReading.getAlertLevel() == BloodPressureReading.AlertLevel.CRITICAL) {
                redirectAttributes.addFlashAttribute("warningMessage", 
                        "CRITICAL BP READING DETECTED! Immediate attention required.");
            }

            redirectAttributes.addFlashAttribute("successMessage", "Blood pressure reading recorded successfully!");
            return "redirect:/bp-readings/patient/" + patientId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error recording reading: " + e.getMessage());
            return "redirect:/bp-readings/patient/" + patientId + "/record";
        }
    }

    /**
     * Display the blood pressure reading details page
     * @param id the reading ID
     * @param model the model
     * @return the blood pressure reading details page view
     */
    @GetMapping("/{id}")
    public String viewReading(@PathVariable Long id, Model model) {
        Optional<BloodPressureReading> readingOpt = bpService.getReadingById(id);

        if (readingOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Reading not found with ID: " + id);
            return "error/not-found";
        }

        model.addAttribute("reading", readingOpt.get());
        return "bp-readings/view";
    }

    /**
     * Display the blood pressure reading edit page
     * @param id the reading ID
     * @param model the model
     * @return the blood pressure reading edit page view
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<BloodPressureReading> readingOpt = bpService.getReadingById(id);

        if (readingOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Reading not found with ID: " + id);
            return "error/not-found";
        }

        model.addAttribute("reading", readingOpt.get());
        return "bp-readings/edit";
    }

    /**
     * Process the blood pressure reading edit form
     * @param id the reading ID
     * @param reading the updated blood pressure reading
     * @param bindingResult the binding result
     * @param redirectAttributes the redirect attributes
     * @return the redirect URL
     */
    @PostMapping("/{id}/edit")
    public String updateReading(
            @PathVariable Long id,
            @Valid @ModelAttribute("reading") BloodPressureReading reading,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "bp-readings/edit";
        }

        try {
            BloodPressureReading updatedReading = bpService.updateReading(id, reading);

            // Check if the reading is critical
            if (updatedReading.getAlertLevel() == BloodPressureReading.AlertLevel.CRITICAL) {
                redirectAttributes.addFlashAttribute("warningMessage", 
                        "CRITICAL BP READING DETECTED! Immediate attention required.");
            }

            redirectAttributes.addFlashAttribute("successMessage", "Blood pressure reading updated successfully!");
            return "redirect:/bp-readings/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating reading: " + e.getMessage());
            return "redirect:/bp-readings/" + id + "/edit";
        }
    }

    /**
     * Delete a blood pressure reading
     * @param id the reading ID
     * @param redirectAttributes the redirect attributes
     * @return the redirect URL
     */
    @PostMapping("/{id}/delete")
    public String deleteReading(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Get the patient ID before deleting the reading
            Long patientId = bpService.getReadingById(id)
                    .map(reading -> reading.getPatient().getId())
                    .orElse(null);

            bpService.deleteReading(id);

            redirectAttributes.addFlashAttribute("successMessage", "Blood pressure reading deleted successfully!");

            // Redirect to the patient's readings page if we have the patient ID
            if (patientId != null) {
                return "redirect:/bp-readings/patient/" + patientId;
            } else {
                return "redirect:/dashboard";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting reading: " + e.getMessage());
            return "redirect:/bp-readings/" + id;
        }
    }

    /**
     * Display the blood pressure chart page for a patient
     * @param patientId the patient ID
     * @param period the time period to display (day, week, month, year)
     * @param model the model
     * @return the blood pressure chart page view
     */
    @GetMapping("/patient/{patientId}/chart")
    public String showChart(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "week") String period,
            Model model) {

        Optional<Patient> patientOpt = patientService.getPatientById(patientId);

        if (patientOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Patient not found with ID: " + patientId);
            return "error/not-found";
        }

        Patient patient = patientOpt.get();
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate;

        // Determine the start date based on the period
        switch (period) {
            case "day":
                startDate = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
                break;
            case "week":
                startDate = endDate.minusWeeks(1);
                break;
            case "month":
                startDate = endDate.minusMonths(1);
                break;
            case "year":
                startDate = endDate.minusYears(1);
                break;
            default:
                startDate = endDate.minusWeeks(1);
                period = "week";
                break;
        }

        List<BloodPressureReading> readings = bpService.getReadingsForPatientBetweenDates(patientId, startDate, endDate);

        model.addAttribute("patient", patient);
        model.addAttribute("readings", readings);
        model.addAttribute("period", period);

        return "bp-readings/chart";
    }
}
