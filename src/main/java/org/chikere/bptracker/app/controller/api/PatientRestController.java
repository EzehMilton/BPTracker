package org.chikere.bptracker.app.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.chikere.bptracker.app.model.Patient;
import org.chikere.bptracker.app.service.PatientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing patients
 */
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientRestController {
    
    private final PatientService patientService;
    
    /**
     * Get all patients
     * @return list of all patients
     */
    @GetMapping
    public ResponseEntity<List<Patient>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }
    
    /**
     * Get paginated patients
     * @param page page number
     * @param size page size
     * @param sortBy field to sort by
     * @param direction sort direction
     * @return page of patients
     */
    @GetMapping("/paged")
    public ResponseEntity<Page<Patient>> getPaginatedPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("DESC") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        
        Page<Patient> patients = patientService.getAllPatients(
                PageRequest.of(page, size, Sort.by(sortDirection, sortBy)));
        
        return ResponseEntity.ok(patients);
    }
    
    /**
     * Get patient by ID
     * @param id patient ID
     * @return patient
     */
    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        return patientService.getPatientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Search patients by name
     * @param searchTerm search term
     * @return list of patients matching the search term
     */
    @GetMapping("/search")
    public ResponseEntity<List<Patient>> searchPatients(@RequestParam String searchTerm) {
        return ResponseEntity.ok(patientService.searchPatientsByName(searchTerm));
    }
    
    /**
     * Create a new patient
     * @param patient patient to create
     * @return created patient
     */
    @PostMapping
    public ResponseEntity<Patient> createPatient(@RequestBody @Valid Patient patient) {
        Patient createdPatient = patientService.createPatient(patient);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPatient);
    }
    
    /**
     * Update an existing patient
     * @param id patient ID
     * @param patient updated patient data
     * @return updated patient
     */
    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(@PathVariable Long id, @RequestBody @Valid Patient patient) {
        try {
            Patient updatedPatient = patientService.updatePatient(id, patient);
            return ResponseEntity.ok(updatedPatient);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Delete a patient
     * @param id patient ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        try {
            patientService.deletePatient(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}