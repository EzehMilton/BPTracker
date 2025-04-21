# Blood Pressure Tracker

A comprehensive web application for healthcare professionals to track and monitor patients' blood pressure readings.

## Overview

Blood Pressure Tracker is a Spring Boot application designed for healthcare professionals such as nurses and support workers to monitor patients' blood pressure readings, identify critical health situations, and manage patient data efficiently.

## Features

- **User Authentication**: Secure login system with role-based access control (Nurse and Support Worker roles)
- **Patient Management**: Add, edit, and view patient information
- **Blood Pressure Monitoring**: Record and track blood pressure readings with additional metrics like heart rate and SpO2
- **Dashboard**: Overview of recent patients, critical readings, and system statistics
- **Alert System**: Automatic alerts for critical blood pressure readings
- **Trend Analysis**: AI-powered analysis to detect abnormal blood pressure trends
- **SMS Notifications**: Automated SMS alerts to patients with critical readings
- **Data Visualization**: Charts and graphs for blood pressure trends

## Technologies Used

- **Backend**:
    - Java 21
    - Spring Boot 3.4.4
    - Spring Security
    - Spring Data JPA
    - Hibernate
    - Lombok

- **Frontend**:
    - Thymeleaf
    - Bootstrap 5.3.2
    - Chart.js 4.3.0

- **Database**:
    - H2 Database (Development)
    - PostgreSQL (Production)

- **Other**:
    - Maven
    - Spring Boot Validation
    - Logging (SLF4J)

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- PostgreSQL (for production deployment)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/bp-tracker.git
   cd bp-tracker
   ```

2. Build the application:
   ```bash
   ./mvnw clean install
   ```

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

4. Access the application at `http://localhost:8080`

### Default Users

The application comes with two default users for testing:

- **Nurse**:
    - Username: `nurse`
    - Password: `password`
    - Role: `NURSE`

- **Support Worker**:
    - Username: `support`
    - Password: `password`
    - Role: `SUPPORT_WORKER`

## Usage

### Dashboard

The dashboard provides an overview of:
- Recently registered patients
- Critical blood pressure readings requiring attention
- Total number of patients in the system

### Patient Management

- Add new patients with their personal and medical information
- View and edit existing patient details
- View a patient's blood pressure history

### Blood Pressure Readings

- Record new blood pressure readings for patients
- Include additional metrics like heart rate, SpO2, and notes
- Specify measurement conditions (arm used, body position, etc.)
- View historical readings with trend analysis

### Alerts

The system automatically:
- Categorizes readings as NORMAL, ELEVATED, HIGH, or CRITICAL
- Sends notifications for critical readings
- Analyzes trends to detect potential health issues

## Project Structure

```
src/main/java/org/chikere/bptracker/app/
├── config/           # Application configuration
├── controller/       # Web controllers
├── model/            # Domain entities
├── repository/       # Data access layer
├── service/          # Business logic
└── AppApplication.java  # Main application class
```

## Configuration

The application can be configured through the `application.properties` file:

- Database settings
- Server port
- Logging levels
- Blood pressure alert thresholds
- AI monitoring schedule
- SMS service configuration

## Development

### Running in Development Mode

The application uses H2 in-memory database by default for development:

```bash
./mvnw spring-boot:run
```

Access the H2 console at `http://localhost:8080/h2-console` with:
- JDBC URL: `jdbc:h2:mem:bptracker`
- Username: `sa`
- Password: `password`

### Production Deployment

For production, configure PostgreSQL in `application.properties` and set the profile to `prod`:

```bash
./mvnw spring-boot:run -Dspring.profiles.active=prod
```

## License

This project is licensed under the [MIT License](LICENSE).

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request