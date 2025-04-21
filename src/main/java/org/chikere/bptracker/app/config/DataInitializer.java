package org.chikere.bptracker.app.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chikere.bptracker.app.model.BloodPressureReading;
import org.chikere.bptracker.app.model.Patient;
import org.chikere.bptracker.app.model.User;
import org.chikere.bptracker.app.service.BloodPressureService;
import org.chikere.bptracker.app.service.PatientService;
import org.chikere.bptracker.app.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Configuration class to initialize sample data for the application
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserService userService;
    private final PatientService patientService;
    private final BloodPressureService bpService;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    /**
     * Initialize sample data for the application
     * @return a CommandLineRunner that initializes the data
     */
    @Bean
    @Profile("!prod")
    public CommandLineRunner initData() {
        return args -> {
            log.info("Initializing sample data...");

            // Create default users
            createUsers();

            // Create sample patients
            createPatients();

            log.info("Sample data initialization complete.");
        };
    }

    /**
     * Create default users
     */
    private void createUsers() {
        // Create a nurse (admin) user if it doesn't exist
        if (!userService.getUserByUsername("nurse").isPresent()) {
            User nurse = new User();
            nurse.setUsername("nurse");
            nurse.setPassword("password");
            nurse.setFullName("Head Nurse");
            nurse.setEmail("nurse@example.com");
            nurse.setRole(User.Role.NURSE);
            nurse.setEnabled(true);

            userService.createUser(nurse);
            log.info("Created default nurse user: username=nurse, password=password");
        }

        // Create a support worker user if it doesn't exist
        if (!userService.getUserByUsername("support").isPresent()) {
            User support = new User();
            support.setUsername("support");
            support.setPassword("password");
            support.setFullName("Support Worker");
            support.setEmail("support@example.com");
            support.setRole(User.Role.SUPPORT_WORKER);
            support.setEnabled(true);

            userService.createUser(support);
            log.info("Created default support worker user: username=support, password=password");
        }
    }

    /**
     * Create sample patients with blood pressure readings
     */
    private void createPatients() {
        // Only create sample patients if none exist
        if (patientService.getAllPatients().isEmpty()) {
            // Get the nurse user for recording BP readings
            User nurse = userService.getUserByUsername("nurse").orElse(null);

            // Create 5 sample patients
            for (int i = 1; i <= 5; i++) {
                Patient patient = createSamplePatient(i);
                Patient savedPatient = patientService.createPatient(patient);

                // Create sample BP readings for each patient
                if (nurse != null) {
                    createSampleReadings(savedPatient, nurse);
                }

                log.info("Created sample patient: {} {}", patient.getFirstName(), patient.getLastName());
            }
        }
    }

    /**
     * Create a sample patient
     * @param index the index of the patient
     * @return the created patient
     */
    private Patient createSamplePatient(int index) {
        Patient patient = new Patient();

        switch (index) {
            case 1:
                patient.setFirstName("John");
                patient.setLastName("Doe");
                patient.setGender(Patient.Gender.MALE);
                patient.setDateOfBirth(LocalDate.of(1970, 5, 15));
                patient.setPhoneNumber("+1234567890");
                patient.setEmail("john.doe@example.com");
                patient.setAddress("123 Main St, Anytown, USA");
                patient.setMedicalHistory("Hypertension, Diabetes");
                break;
            case 2:
                patient.setFirstName("Jane");
                patient.setLastName("Smith");
                patient.setGender(Patient.Gender.FEMALE);
                patient.setDateOfBirth(LocalDate.of(1985, 8, 22));
                patient.setPhoneNumber("+1987654321");
                patient.setEmail("jane.smith@example.com");
                patient.setAddress("456 Oak Ave, Somewhere, USA");
                patient.setMedicalHistory("Asthma");
                break;
            case 3:
                patient.setFirstName("Robert");
                patient.setLastName("Johnson");
                patient.setGender(Patient.Gender.MALE);
                patient.setDateOfBirth(LocalDate.of(1962, 3, 10));
                patient.setPhoneNumber("+1122334455");
                patient.setEmail("robert.johnson@example.com");
                patient.setAddress("789 Pine Rd, Nowhere, USA");
                patient.setMedicalHistory("Heart disease, High cholesterol");
                break;
            case 4:
                patient.setFirstName("Emily");
                patient.setLastName("Williams");
                patient.setGender(Patient.Gender.FEMALE);
                patient.setDateOfBirth(LocalDate.of(1990, 11, 5));
                patient.setPhoneNumber("+1555666777");
                patient.setEmail("emily.williams@example.com");
                patient.setAddress("321 Elm St, Anyplace, USA");
                patient.setMedicalHistory("None");
                break;
            case 5:
                patient.setFirstName("Michael");
                patient.setLastName("Brown");
                patient.setGender(Patient.Gender.MALE);
                patient.setDateOfBirth(LocalDate.of(1978, 7, 30));
                patient.setPhoneNumber("+1999888777");
                patient.setEmail("michael.brown@example.com");
                patient.setAddress("654 Maple Ave, Somewhere, USA");
                patient.setMedicalHistory("Obesity, Sleep apnea");
                break;
            default:
                patient.setFirstName("Patient");
                patient.setLastName("Number " + index);
                patient.setGender(Patient.Gender.OTHER);
                patient.setDateOfBirth(LocalDate.now().minusYears(30 + random.nextInt(40)));
                patient.setPhoneNumber("+1" + (1000000000 + random.nextInt(9000000)));
                patient.setEmail("patient" + index + "@example.com");
                patient.setAddress(index + " Some St, Anytown, USA");
                patient.setMedicalHistory("Sample medical history");
                break;
        }

        return patient;
    }

    /**
     * Create sample blood pressure readings for a patient
     * @param patient the patient
     * @param recordedBy the user who recorded the readings
     */
    private void createSampleReadings(Patient patient, User recordedBy) {
        // Create 5 readings for each patient over the last 30 days
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 5; i++) {
            BloodPressureReading reading = new BloodPressureReading();

            // Set random but realistic values
            int systolic, diastolic;

            // For patient 3 (Robert Johnson), create some critical readings
            if (patient.getLastName().equals("Johnson") && i >= 3) {
                systolic = 180 + random.nextInt(20);
                diastolic = 120 + random.nextInt(10);
            } else {
                systolic = 110 + random.nextInt(60);
                diastolic = 70 + random.nextInt(30);
            }

            reading.setSystolicPressure(systolic);
            reading.setDiastolicPressure(diastolic);
            reading.setPulse(60 + random.nextInt(40));
            reading.setNotes("Sample reading " + (i + 1));
            reading.setReadingTime(now.minusDays(i * 5 + random.nextInt(5)));

            // Save the reading
            bpService.createReading(patient.getId(), reading, recordedBy);

            log.debug("Created BP reading for {}: {}/{}", 
                    patient.getLastName(), reading.getSystolicPressure(), reading.getDiastolicPressure());
        }
    }
}
