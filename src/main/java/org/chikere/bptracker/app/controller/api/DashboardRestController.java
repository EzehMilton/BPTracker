package org.chikere.bptracker.app.controller.api;

import lombok.RequiredArgsConstructor;
import org.chikere.bptracker.app.model.BloodPressureReading;
import org.chikere.bptracker.app.model.Patient;
import org.chikere.bptracker.app.model.User;
import org.chikere.bptracker.app.service.BloodPressureService;
import org.chikere.bptracker.app.service.PatientService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for dashboard data
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardRestController {
    
    private final PatientService patientService;
    private final BloodPressureService bpService;
    
    /**
     * Get dashboard data
     * @param authentication current user authentication
     * @return dashboard data
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboardData(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        // Get the 5 most recently registered patients
        List<Patient> recentPatients = patientService.getAllPatients(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "registrationDate"))).getContent();
        
        // Count total patients
        long totalPatients = patientService.getAllPatients().size();
        
        // Get critical readings (if any)
        List<BloodPressureReading> criticalReadings = getCriticalReadings();
        
        Map<String, Object> dashboardData = new HashMap<>();
        dashboardData.put("user", currentUser);
        dashboardData.put("recentPatients", recentPatients);
        dashboardData.put("totalPatients", totalPatients);
        dashboardData.put("criticalReadings", criticalReadings);
        dashboardData.put("hasCriticalReadings", !criticalReadings.isEmpty());
        
        return ResponseEntity.ok(dashboardData);
    }
    
    /**
     * Get summary statistics
     * @return summary statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        // Count total patients
        long totalPatients = patientService.getAllPatients().size();
        
        // Count critical readings
        List<BloodPressureReading> criticalReadings = getCriticalReadings();
        
        // Get patient distribution by gender
        List<Patient> allPatients = patientService.getAllPatients();
        Map<Patient.Gender, Long> patientsByGender = allPatients.stream()
                .collect(Collectors.groupingBy(Patient::getGender, Collectors.counting()));
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPatients", totalPatients);
        stats.put("criticalReadingsCount", criticalReadings.size());
        stats.put("patientsByGender", patientsByGender);
        
        return ResponseEntity.ok(stats);
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