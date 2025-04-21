package org.chikere.bptracker.app.repository;

import org.chikere.bptracker.app.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Patient entity
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    
    /**
     * Find patients by last name
     * @param lastName the last name to search for
     * @return a list of patients with the given last name
     */
    List<Patient> findByLastNameContainingIgnoreCase(String lastName);
    
    /**
     * Find patients by first name
     * @param firstName the first name to search for
     * @return a list of patients with the given first name
     */
    List<Patient> findByFirstNameContainingIgnoreCase(String firstName);
    
    /**
     * Find patients by phone number
     * @param phoneNumber the phone number to search for
     * @return a list of patients with the given phone number
     */
    List<Patient> findByPhoneNumber(String phoneNumber);
    
    /**
     * Find patients by email
     * @param email the email to search for
     * @return a list of patients with the given email
     */
    List<Patient> findByEmailContainingIgnoreCase(String email);
    
    /**
     * Search for patients by name (first name or last name)
     * @param searchTerm the search term
     * @return a list of patients matching the search term
     */
    @Query("SELECT p FROM Patient p WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Patient> searchByName(String searchTerm);
}