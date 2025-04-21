package org.chikere.bptracker.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chikere.bptracker.app.model.BloodPressureReading;
import org.chikere.bptracker.app.model.Patient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * Service for AI monitoring of blood pressure readings (mocked implementation)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIMonitoringService {

    private final PatientService patientService;
    private final BloodPressureService bloodPressureService;
    private final AlertService alertService;
    private final Random random = new Random();

    /**
     * Scheduled task to analyze blood pressure readings for all patients
     * This is scheduled using the cron expression defined in application.properties
     */
    @Scheduled(cron = "${app.ai.monitoring.schedule:0 0 */6 * * *}")
    public void analyzeAllPatients() {
        log.info("Starting scheduled AI analysis of blood pressure readings for all patients");
        
        List<Patient> patients = patientService.getAllPatients();
        log.info("Found {} patients to analyze", patients.size());
        
        for (Patient patient : patients) {
            try {
                analyzePatient(patient);
            } catch (Exception e) {
                log.error("Error analyzing patient {}: {}", patient.getId(), e.getMessage(), e);
            }
        }
        
        log.info("Completed scheduled AI analysis");
    }

    /**
     * Analyze blood pressure readings for a specific patient
     * @param patient the patient to analyze
     */
    public void analyzePatient(Patient patient) {
        log.info("Analyzing blood pressure readings for patient: {} {}", patient.getFirstName(), patient.getLastName());
        
        // Get readings from the last 30 days
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);
        
        List<BloodPressureReading> readings = bloodPressureService.getReadingsForPatientBetweenDates(
                patient.getId(), startDate, endDate);
        
        if (readings.isEmpty()) {
            log.info("No readings found for patient {} in the last 30 days", patient.getId());
            return;
        }
        
        log.info("Found {} readings for patient {} in the last 30 days", readings.size(), patient.getId());
        
        // Mock AI analysis
        String analysisResult = mockAIAnalysis(readings);
        
        // If abnormal trend detected, trigger an alert
        if (analysisResult != null) {
            log.warn("Abnormal BP trend detected for patient {}: {}", patient.getId(), analysisResult);
            alertService.triggerAbnormalTrendAlert(patient, analysisResult);
        } else {
            log.info("No abnormal BP trends detected for patient {}", patient.getId());
        }
    }

    /**
     * Mock AI analysis of blood pressure readings
     * @param readings the readings to analyze
     * @return a description of the abnormal trend if detected, or null if no abnormal trend detected
     */
    private String mockAIAnalysis(List<BloodPressureReading> readings) {
        // In a real implementation, this would use a machine learning model to analyze the readings
        // For now, we just randomly decide whether to trigger an alert
        
        // Only trigger an alert if there are at least 5 readings
        if (readings.size() < 5) {
            return null;
        }
        
        // 20% chance of detecting an abnormal trend
        if (random.nextInt(5) == 0) {
            // Randomly select one of several possible abnormal trends
            String[] abnormalTrends = {
                "Consistently elevated systolic pressure over the last week",
                "Significant increase in diastolic pressure compared to baseline",
                "High variability in readings suggesting unstable blood pressure",
                "Gradual upward trend in both systolic and diastolic pressure",
                "Elevated readings predominantly in the morning, suggesting morning hypertension"
            };
            
            return abnormalTrends[random.nextInt(abnormalTrends.length)];
        }
        
        return null;
    }
}