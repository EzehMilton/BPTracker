package org.chikere.bptracker.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a patient in the system
 */
@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be valid")
    @Column(name = "phone_number")
    private String phoneNumber;

    @Email(message = "Email should be valid")
    private String email;

    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;

    @Size(max = 1000, message = "Medical history cannot exceed 1000 characters")
    @Column(name = "medical_history", length = 1000)
    private String medicalHistory;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate = LocalDate.now();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BloodPressureReading> bpReadings = new ArrayList<>();

    /**
     * Enum representing gender options
     */
    public enum Gender {
        MALE,
        FEMALE,
        OTHER,
        PREFER_NOT_TO_SAY
    }

    /**
     * Helper method to add a BP reading to this patient
     * @param reading The BP reading to add
     */
    public void addBpReading(BloodPressureReading reading) {
        bpReadings.add(reading);
        reading.setPatient(this);
    }

    /**
     * Helper method to remove a BP reading from this patient
     * @param reading The BP reading to remove
     */
    public void removeBpReading(BloodPressureReading reading) {
        bpReadings.remove(reading);
        reading.setPatient(null);
    }
}