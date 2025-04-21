package org.chikere.bptracker.app.repository;

import org.chikere.bptracker.app.model.BloodPressureReading;
import org.chikere.bptracker.app.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for BloodPressureReading entity
 */
@Repository
public interface BloodPressureReadingRepository extends JpaRepository<BloodPressureReading, Long> {
    
    /**
     * Find all readings for a patient
     * @param patient the patient
     * @return a list of blood pressure readings for the patient
     */
    List<BloodPressureReading> findByPatient(Patient patient);
    
    /**
     * Find all readings for a patient, paginated
     * @param patient the patient
     * @param pageable pagination information
     * @return a page of blood pressure readings for the patient
     */
    Page<BloodPressureReading> findByPatient(Patient patient, Pageable pageable);
    
    /**
     * Find all readings for a patient, ordered by reading time descending
     * @param patient the patient
     * @return a list of blood pressure readings for the patient, ordered by reading time descending
     */
    List<BloodPressureReading> findByPatientOrderByReadingTimeDesc(Patient patient);
    
    /**
     * Find all readings for a patient between two dates
     * @param patient the patient
     * @param startDate the start date
     * @param endDate the end date
     * @return a list of blood pressure readings for the patient between the two dates
     */
    List<BloodPressureReading> findByPatientAndReadingTimeBetween(Patient patient, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find all critical readings for a patient
     * @param patient the patient
     * @param alertLevel the alert level
     * @return a list of critical blood pressure readings for the patient
     */
    List<BloodPressureReading> findByPatientAndAlertLevel(Patient patient, BloodPressureReading.AlertLevel alertLevel);
    
    /**
     * Find the latest reading for a patient
     * @param patient the patient
     * @return the latest blood pressure reading for the patient
     */
    @Query("SELECT r FROM BloodPressureReading r WHERE r.patient = :patient ORDER BY r.readingTime DESC")
    List<BloodPressureReading> findLatestByPatient(Patient patient, Pageable pageable);
    
    /**
     * Count the number of readings for a patient
     * @param patient the patient
     * @return the number of readings for the patient
     */
    long countByPatient(Patient patient);
}