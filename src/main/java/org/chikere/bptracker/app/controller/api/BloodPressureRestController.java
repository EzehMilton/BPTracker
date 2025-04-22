package org.chikere.bptracker.app.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.chikere.bptracker.app.model.BloodPressureReading;
import org.chikere.bptracker.app.model.User;
import org.chikere.bptracker.app.service.BloodPressureService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing blood pressure readings
 */
@RestController
@RequestMapping("/api/readings")
@RequiredArgsConstructor
public class BloodPressureRestController {
    
    private final BloodPressureService bpService;
    
    /**
     * Get all readings
     * @return list of all readings
     */
    @GetMapping
    public ResponseEntity<List<BloodPressureReading>> getAllReadings() {
        return ResponseEntity.ok(bpService.getAllReadings());
    }
    
    /**
     * Get paginated readings
     * @param page page number
     * @param size page size
     * @param sortBy field to sort by
     * @param direction sort direction
     * @return page of readings
     */
    @GetMapping("/paged")
    public ResponseEntity<Page<BloodPressureReading>> getPaginatedReadings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "recordedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("DESC") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        
        Page<BloodPressureReading> readings = bpService.getAllReadings(
                PageRequest.of(page, size, Sort.by(sortDirection, sortBy)));
        
        return ResponseEntity.ok(readings);
    }
    
    /**
     * Get reading by ID
     * @param id reading ID
     * @return reading
     */
    @GetMapping("/{id}")
    public ResponseEntity<BloodPressureReading> getReadingById(@PathVariable Long id) {
        return bpService.getReadingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get readings for a patient
     * @param patientId patient ID
     * @return list of readings for the patient
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<BloodPressureReading>> getReadingsForPatient(@PathVariable Long patientId) {
        try {
            return ResponseEntity.ok(bpService.getReadingsForPatient(patientId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get paginated readings for a patient
     * @param patientId patient ID
     * @param page page number
     * @param size page size
     * @return page of readings for the patient
     */
    @GetMapping("/patient/{patientId}/paged")
    public ResponseEntity<Page<BloodPressureReading>> getPaginatedReadingsForPatient(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Page<BloodPressureReading> readings = bpService.getReadingsForPatient(
                    patientId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "recordedAt")));
            return ResponseEntity.ok(readings);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get latest reading for a patient
     * @param patientId patient ID
     * @return latest reading for the patient
     */
    @GetMapping("/patient/{patientId}/latest")
    public ResponseEntity<BloodPressureReading> getLatestReadingForPatient(@PathVariable Long patientId) {
        try {
            BloodPressureReading reading = bpService.getLatestReadingForPatient(patientId);
            return reading != null ? 
                    ResponseEntity.ok(reading) : 
                    ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get readings for a patient between dates
     * @param patientId patient ID
     * @param startDate start date
     * @param endDate end date
     * @return list of readings for the patient between the dates
     */
    @GetMapping("/patient/{patientId}/between")
    public ResponseEntity<List<BloodPressureReading>> getReadingsForPatientBetweenDates(
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            return ResponseEntity.ok(bpService.getReadingsForPatientBetweenDates(patientId, startDate, endDate));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get critical readings for a patient
     * @param patientId patient ID
     * @return list of critical readings for the patient
     */
    @GetMapping("/patient/{patientId}/critical")
    public ResponseEntity<List<BloodPressureReading>> getCriticalReadingsForPatient(@PathVariable Long patientId) {
        try {
            return ResponseEntity.ok(bpService.getCriticalReadingsForPatient(patientId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Create a new reading
     * @param patientId patient ID
     * @param reading reading to create
     * @param authentication current user authentication
     * @return created reading
     */
    @PostMapping("/patient/{patientId}")
    public ResponseEntity<BloodPressureReading> createReading(
            @PathVariable Long patientId,
            @RequestBody @Valid BloodPressureReading reading,
            Authentication authentication) {
        
        try {
            User currentUser = (User) authentication.getPrincipal();
            BloodPressureReading createdReading = bpService.createReading(patientId, reading, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReading);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update an existing reading
     * @param id reading ID
     * @param reading updated reading data
     * @return updated reading
     */
    @PutMapping("/{id}")
    public ResponseEntity<BloodPressureReading> updateReading(
            @PathVariable Long id,
            @RequestBody @Valid BloodPressureReading reading) {
        
        try {
            BloodPressureReading updatedReading = bpService.updateReading(id, reading);
            return ResponseEntity.ok(updatedReading);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Delete a reading
     * @param id reading ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReading(@PathVariable Long id) {
        try {
            bpService.deleteReading(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get count of readings for a patient
     * @param patientId patient ID
     * @return count of readings
     */
    @GetMapping("/patient/{patientId}/count")
    public ResponseEntity<Map<String, Long>> getReadingCountForPatient(@PathVariable Long patientId) {
        try {
            long count = bpService.countReadingsForPatient(patientId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}