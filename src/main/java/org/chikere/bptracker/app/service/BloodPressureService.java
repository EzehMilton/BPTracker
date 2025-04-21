package org.chikere.bptracker.app.service;

import lombok.RequiredArgsConstructor;
import org.chikere.bptracker.app.model.BloodPressureReading;
import org.chikere.bptracker.app.model.Patient;
import org.chikere.bptracker.app.model.User;
import org.chikere.bptracker.app.repository.BloodPressureReadingRepository;
import org.chikere.bptracker.app.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing blood pressure readings
 */
@Service
@RequiredArgsConstructor
public class BloodPressureService {

    private final BloodPressureReadingRepository bpRepository;
    private final PatientRepository patientRepository;
    private final AlertService alertService;

    @Value("${app.bp.critical.systolic:180}")
    private int criticalSystolicThreshold;

    @Value("${app.bp.critical.diastolic:120}")
    private int criticalDiastolicThreshold;

    /**
     * Get all blood pressure readings
     * @return a list of all blood pressure readings
     */
    public List<BloodPressureReading> getAllReadings() {
        return bpRepository.findAll();
    }

    /**
     * Get all blood pressure readings, paginated
     * @param pageable pagination information
     * @return a page of blood pressure readings
     */
    public Page<BloodPressureReading> getAllReadings(Pageable pageable) {
        return bpRepository.findAll(pageable);
    }

    /**
     * Get a blood pressure reading by ID
     * @param id the reading ID
     * @return an Optional containing the reading if found, or empty if not found
     */
    public Optional<BloodPressureReading> getReadingById(Long id) {
        return bpRepository.findById(id);
    }

    /**
     * Get all readings for a patient
     * @param patientId the patient ID
     * @return a list of blood pressure readings for the patient
     */
    public List<BloodPressureReading> getReadingsForPatient(Long patientId) {
        return patientRepository.findById(patientId)
                .map(bpRepository::findByPatient)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with id: " + patientId));
    }

    /**
     * Get all readings for a patient, paginated
     * @param patientId the patient ID
     * @param pageable pagination information
     * @return a page of blood pressure readings for the patient
     */
    public Page<BloodPressureReading> getReadingsForPatient(Long patientId, Pageable pageable) {
        return patientRepository.findById(patientId)
                .map(patient -> bpRepository.findByPatient(patient, pageable))
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with id: " + patientId));
    }

    /**
     * Get all readings for a patient, ordered by reading time descending
     * @param patientId the patient ID
     * @return a list of blood pressure readings for the patient, ordered by reading time descending
     */
    public List<BloodPressureReading> getReadingsForPatientOrderedByTimeDesc(Long patientId) {
        return patientRepository.findById(patientId)
                .map(bpRepository::findByPatientOrderByReadingTimeDesc)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with id: " + patientId));
    }

    /**
     * Get the latest reading for a patient
     * @param patientId the patient ID
     * @return the latest blood pressure reading for the patient, or null if none exists
     */
    public BloodPressureReading getLatestReadingForPatient(Long patientId) {
        return patientRepository.findById(patientId)
                .map(patient -> {
                    List<BloodPressureReading> readings = bpRepository.findLatestByPatient(
                            patient, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "readingTime")));
                    return readings.isEmpty() ? null : readings.get(0);
                })
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with id: " + patientId));
    }

    /**
     * Get all readings for a patient between two dates
     * @param patientId the patient ID
     * @param startDate the start date
     * @param endDate the end date
     * @return a list of blood pressure readings for the patient between the two dates
     */
    public List<BloodPressureReading> getReadingsForPatientBetweenDates(Long patientId, LocalDateTime startDate, LocalDateTime endDate) {
        return patientRepository.findById(patientId)
                .map(patient -> bpRepository.findByPatientAndReadingTimeBetween(patient, startDate, endDate))
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with id: " + patientId));
    }

    /**
     * Get all critical readings for a patient
     * @param patientId the patient ID
     * @return a list of critical blood pressure readings for the patient
     */
    public List<BloodPressureReading> getCriticalReadingsForPatient(Long patientId) {
        return patientRepository.findById(patientId)
                .map(patient -> bpRepository.findByPatientAndAlertLevel(patient, BloodPressureReading.AlertLevel.CRITICAL))
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with id: " + patientId));
    }

    /**
     * Create a new blood pressure reading
     * @param patientId the patient ID
     * @param reading the reading to create
     * @param recordedBy the user who recorded the reading
     * @return the created reading
     */
    @Transactional
    public BloodPressureReading createReading(Long patientId, BloodPressureReading reading, User recordedBy) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with id: " + patientId));

        reading.setPatient(patient);
        reading.setRecordedBy(recordedBy);
        reading.setReadingTime(LocalDateTime.now());

        // Calculate alert level
        reading.calculateAlertLevel();

        BloodPressureReading savedReading = bpRepository.save(reading);

        // Check if the reading is critical and trigger an alert if necessary
        if (reading.isCritical(criticalSystolicThreshold, criticalDiastolicThreshold)) {
            alertService.triggerHighBPAlert(savedReading);
        }

        return savedReading;
    }

    /**
     * Update an existing blood pressure reading
     * @param id the reading ID
     * @param updatedReading the updated reading data
     * @return the updated reading
     */
    @Transactional
    public BloodPressureReading updateReading(Long id, BloodPressureReading updatedReading) {
        return bpRepository.findById(id)
                .map(reading -> {
                    // Update fields
                    reading.setSystolicPressure(updatedReading.getSystolicPressure());
                    reading.setDiastolicPressure(updatedReading.getDiastolicPressure());
                    reading.setHeartRate(updatedReading.getHeartRate());
                    reading.setWeightKg(updatedReading.getWeightKg());
                    reading.setSpo2Percentage(updatedReading.getSpo2Percentage());
                    reading.setMeasurementMethod(updatedReading.getMeasurementMethod());
                    reading.setArmUsed(updatedReading.getArmUsed());
                    reading.setBodyPosition(updatedReading.getBodyPosition());
                    reading.setActivityBefore(updatedReading.getActivityBefore());
                    reading.setDeviceId(updatedReading.getDeviceId());
                    reading.setDeviceModel(updatedReading.getDeviceModel());
                    reading.setNotes(updatedReading.getNotes());

                    // Recalculate alert level
                    reading.calculateAlertLevel();

                    BloodPressureReading savedReading = bpRepository.save(reading);

                    // Check if the reading is critical and trigger an alert if necessary
                    if (reading.isCritical(criticalSystolicThreshold, criticalDiastolicThreshold)) {
                        alertService.triggerHighBPAlert(savedReading);
                    }

                    return savedReading;
                })
                .orElseThrow(() -> new IllegalArgumentException("Reading not found with id: " + id));
    }

    /**
     * Delete a blood pressure reading
     * @param id the reading ID
     */
    @Transactional
    public void deleteReading(Long id) {
        bpRepository.deleteById(id);
    }

    /**
     * Count the number of readings for a patient
     * @param patientId the patient ID
     * @return the number of readings for the patient
     */
    public long countReadingsForPatient(Long patientId) {
        return patientRepository.findById(patientId)
                .map(bpRepository::countByPatient)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with id: " + patientId));
    }
}
