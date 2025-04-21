package org.chikere.bptracker.app.controller;

import lombok.RequiredArgsConstructor;
import org.chikere.bptracker.app.model.BloodPressureReading;
import org.chikere.bptracker.app.model.Patient;
import org.chikere.bptracker.app.model.User;
import org.chikere.bptracker.app.service.BloodPressureService;
import org.chikere.bptracker.app.service.PatientService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controller for handling dashboard-related requests
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final PatientService patientService;
    private final BloodPressureService bpService;

    /**
     * Display the main dashboard page
     * @param model the model
     * @return the dashboard page view
     */
    @GetMapping({"/", "/dashboard"})
    public String showDashboard(Model model) {
        // Get the current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        // Get the 5 most recently registered patients
        List<Patient> recentPatients = patientService.getAllPatients(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "registrationDate"))).getContent();
        
        // Count total patients
        long totalPatients = patientService.getAllPatients().size();
        
        // Get critical readings (if any)
        List<BloodPressureReading> criticalReadings = getCriticalReadings();
        
        model.addAttribute("user", currentUser);
        model.addAttribute("recentPatients", recentPatients);
        model.addAttribute("totalPatients", totalPatients);
        model.addAttribute("criticalReadings", criticalReadings);
        model.addAttribute("hasCriticalReadings", !criticalReadings.isEmpty());
        
        return "dashboard";
    }

    /**
     * Get critical blood pressure readings
     * @return a list of critical blood pressure readings
     */
    private List<BloodPressureReading> getCriticalReadings() {
        // In a real application, we would query for all critical readings
        // For simplicity, we'll just get the first 10 patients and check their readings
        List<Patient> patients = patientService.getAllPatients(
                PageRequest.of(0, 10)).getContent();
        
        return patients.stream()
                .flatMap(patient -> {
                    try {
                        return bpService.getCriticalReadingsForPatient(patient.getId()).stream();
                    } catch (Exception e) {
                        return java.util.stream.Stream.empty();
                    }
                })
                .limit(5)
                .toList();
    }
}