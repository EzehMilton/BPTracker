package org.chikere.bptracker.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a blood pressure reading for a patient
 */
@Entity
@Table(name = "blood_pressure_readings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BloodPressureReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull(message = "Systolic pressure is required")
    @Min(value = 60, message = "Systolic pressure must be at least 60")
    @Max(value = 250, message = "Systolic pressure must be at most 250")
    @Column(name = "systolic", nullable = false)
    private Integer systolicPressure;

    @NotNull(message = "Diastolic pressure is required")
    @Min(value = 40, message = "Diastolic pressure must be at least 40")
    @Max(value = 150, message = "Diastolic pressure must be at most 150")
    @Column(name = "diastolic", nullable = false)
    private Integer diastolicPressure;

    @Min(value = 40, message = "Heart rate must be at least 40")
    @Max(value = 200, message = "Heart rate must be at most 200")
    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "spo2_percentage", precision = 4, scale = 1)
    private BigDecimal spo2Percentage;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Column(length = 1000)
    private String notes;

    @NotNull(message = "Reading time is required")
    @Column(name = "reading_time", nullable = false)
    private LocalDateTime readingTime = LocalDateTime.now();

    @Size(max = 50)
    @Column(name = "measurement_method", length = 50)
    private String measurementMethod;

    @Size(max = 10)
    @Column(name = "arm_used", length = 10)
    private String armUsed;

    @Size(max = 20)
    @Column(name = "body_position", length = 20)
    private String bodyPosition;

    @Size(max = 50)
    @Column(name = "activity_before", length = 50)
    private String activityBefore;

    @Size(max = 50)
    @Column(name = "device_id", length = 50)
    private String deviceId;

    @Size(max = 100)
    @Column(name = "device_model", length = 100)
    private String deviceModel;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private User recordedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_level")
    private AlertLevel alertLevel;

    public void setPulse(int i) {

    }

    /**
     * Enum representing the alert level for a blood pressure reading
     */
    public enum AlertLevel {
        NORMAL,
        ELEVATED,
        HIGH,
        CRITICAL
    }

    /**
     * Determines if this reading is critical based on configured thresholds
     * @param systolicThreshold The systolic threshold for critical readings
     * @param diastolicThreshold The diastolic threshold for critical readings
     * @return true if the reading is critical, false otherwise
     */
    public boolean isCritical(int systolicThreshold, int diastolicThreshold) {
        return systolicPressure >= systolicThreshold || diastolicPressure >= diastolicThreshold;
    }

    /**
     * Sets the appropriate alert level based on the blood pressure values
     */
    public void calculateAlertLevel() {
        if (systolicPressure >= 180 || diastolicPressure >= 120) {
            alertLevel = AlertLevel.CRITICAL;
        } else if (systolicPressure >= 140 || diastolicPressure >= 90) {
            alertLevel = AlertLevel.HIGH;
        } else if (systolicPressure >= 120 && systolicPressure < 140 && diastolicPressure < 90) {
            alertLevel = AlertLevel.ELEVATED;
        } else {
            alertLevel = AlertLevel.NORMAL;
        }
    }
}
