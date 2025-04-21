package org.chikere.bptracker.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chikere.bptracker.app.model.BloodPressureReading;
import org.chikere.bptracker.app.model.Patient;
import org.springframework.stereotype.Service;

/**
 * Service for handling alerts related to blood pressure readings
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final SMSService smsService;

    /**
     * Trigger an alert for a high blood pressure reading
     * @param reading the blood pressure reading that triggered the alert
     */
    public void triggerHighBPAlert(BloodPressureReading reading) {
        Patient patient = reading.getPatient();
        String message = String.format(
                "ALERT: Critical BP reading detected for %s %s. Systolic: %d, Diastolic: %d. Immediate attention required.",
                patient.getFirstName(),
                patient.getLastName(),
                reading.getSystolicPressure(),
                reading.getDiastolicPressure()
        );
        
        // Log the alert
        log.warn(message);
        
        // Send SMS notification if patient has a phone number
        if (patient.getPhoneNumber() != null && !patient.getPhoneNumber().isEmpty()) {
            smsService.sendSMS(patient.getPhoneNumber(), message);
        }
    }

    /**
     * Trigger an alert for abnormal BP trends detected by AI
     * @param patient the patient with abnormal BP trends
     * @param analysisResult the result of the AI analysis
     */
    public void triggerAbnormalTrendAlert(Patient patient, String analysisResult) {
        String message = String.format(
                "ALERT: Abnormal BP trend detected for %s %s. %s",
                patient.getFirstName(),
                patient.getLastName(),
                analysisResult
        );
        
        // Log the alert
        log.warn(message);
        
        // Send SMS notification if patient has a phone number
        if (patient.getPhoneNumber() != null && !patient.getPhoneNumber().isEmpty()) {
            String smsMessage = String.format(
                    "Dear %s, our monitoring system has detected an abnormal trend in your blood pressure readings. " +
                    "Please consult with your healthcare provider as soon as possible.",
                    patient.getFirstName()
            );
            smsService.sendSMS(patient.getPhoneNumber(), smsMessage);
        }
    }
}