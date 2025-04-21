package org.chikere.bptracker.app.service;

import lombok.RequiredArgsConstructor;
import org.chikere.bptracker.app.model.Patient;
import org.chikere.bptracker.app.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing patients
 */
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    /**
     * Get all patients
     * @return a list of all patients
     */
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    /**
     * Get all patients, paginated
     * @param pageable pagination information
     * @return a page of patients
     */
    public Page<Patient> getAllPatients(Pageable pageable) {
        return patientRepository.findAll(pageable);
    }

    /**
     * Get a patient by ID
     * @param id the patient ID
     * @return an Optional containing the patient if found, or empty if not found
     */
    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    /**
     * Search for patients by name
     * @param searchTerm the search term
     * @return a list of patients matching the search term
     */
    public List<Patient> searchPatientsByName(String searchTerm) {
        return patientRepository.searchByName(searchTerm);
    }

    /**
     * Find patients by last name
     * @param lastName the last name to search for
     * @return a list of patients with the given last name
     */
    public List<Patient> findPatientsByLastName(String lastName) {
        return patientRepository.findByLastNameContainingIgnoreCase(lastName);
    }

    /**
     * Find patients by first name
     * @param firstName the first name to search for
     * @return a list of patients with the given first name
     */
    public List<Patient> findPatientsByFirstName(String firstName) {
        return patientRepository.findByFirstNameContainingIgnoreCase(firstName);
    }

    /**
     * Find patients by phone number
     * @param phoneNumber the phone number to search for
     * @return a list of patients with the given phone number
     */
    public List<Patient> findPatientsByPhoneNumber(String phoneNumber) {
        return patientRepository.findByPhoneNumber(phoneNumber);
    }

    /**
     * Find patients by email
     * @param email the email to search for
     * @return a list of patients with the given email
     */
    public List<Patient> findPatientsByEmail(String email) {
        return patientRepository.findByEmailContainingIgnoreCase(email);
    }

    /**
     * Create a new patient
     * @param patient the patient to create
     * @return the created patient
     */
    @Transactional
    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    /**
     * Update an existing patient
     * @param id the patient ID
     * @param updatedPatient the updated patient data
     * @return the updated patient
     */
    @Transactional
    public Patient updatePatient(Long id, Patient updatedPatient) {
        return patientRepository.findById(id)
                .map(patient -> {
                    // Update fields
                    patient.setFirstName(updatedPatient.getFirstName());
                    patient.setLastName(updatedPatient.getLastName());
                    patient.setDateOfBirth(updatedPatient.getDateOfBirth());
                    patient.setGender(updatedPatient.getGender());
                    patient.setPhoneNumber(updatedPatient.getPhoneNumber());
                    patient.setEmail(updatedPatient.getEmail());
                    patient.setAddress(updatedPatient.getAddress());
                    patient.setMedicalHistory(updatedPatient.getMedicalHistory());
                    
                    return patientRepository.save(patient);
                })
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with id: " + id));
    }

    /**
     * Delete a patient
     * @param id the patient ID
     */
    @Transactional
    public void deletePatient(Long id) {
        patientRepository.deleteById(id);
    }
}