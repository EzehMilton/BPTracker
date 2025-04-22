# Blood Pressure Tracker - Mobile-Only Approach

## Overview

The Blood Pressure Tracker application has been modified to support only a mobile interface using Flutter. This document explains the changes made and provides guidance on using the application with a Flutter frontend.

## Changes Made

The following changes have been made to the application:

1. **Removed Thymeleaf Dependencies**: All Thymeleaf dependencies have been removed from the project.
2. **Removed Thymeleaf Templates**: All Thymeleaf templates have been removed from the project.
3. **Removed Traditional MVC Controllers**: All controllers that returned view names have been removed.
4. **Updated Security Configuration**: The security configuration has been updated to handle only API requests.
5. **Retained REST API Endpoints**: All REST API endpoints have been retained to support the Flutter frontend.

## Architecture

The application now follows a client-server architecture:

- **Backend**: Spring Boot application providing REST API endpoints
- **Frontend**: Flutter application consuming the REST API

## API Endpoints

The following API endpoints are available:

### Authentication

- **POST /api/auth/login**: Authenticate user and get JWT token
  - Request: `{ "username": "user", "password": "password" }`
  - Response: `{ "token": "jwt-token" }`

- **POST /api/auth/register**: Register a new user
  - Request: `{ "username": "newuser", "password": "password", "email": "user@example.com", "fullName": "New User" }`
  - Response: `"User registered successfully"`

### Patients

- **GET /api/patients**: Get all patients
- **GET /api/patients/paged**: Get paginated patients
- **GET /api/patients/{id}**: Get patient by ID
- **GET /api/patients/search**: Search patients by name
- **POST /api/patients**: Create a new patient
- **PUT /api/patients/{id}**: Update an existing patient
- **DELETE /api/patients/{id}**: Delete a patient

### Blood Pressure Readings

- **GET /api/readings**: Get all readings
- **GET /api/readings/paged**: Get paginated readings
- **GET /api/readings/{id}**: Get reading by ID
- **GET /api/readings/patient/{patientId}**: Get readings for a patient
- **GET /api/readings/patient/{patientId}/paged**: Get paginated readings for a patient
- **GET /api/readings/patient/{patientId}/latest**: Get latest reading for a patient
- **GET /api/readings/patient/{patientId}/between**: Get readings for a patient between dates
- **GET /api/readings/patient/{patientId}/critical**: Get critical readings for a patient
- **GET /api/readings/patient/{patientId}/count**: Get count of readings for a patient
- **POST /api/readings/patient/{patientId}**: Create a new reading
- **PUT /api/readings/{id}**: Update an existing reading
- **DELETE /api/readings/{id}**: Delete a reading

### Users

- **GET /api/users**: Get all users (admin only)
- **GET /api/users/{id}**: Get user by ID (admin or self)
- **GET /api/users/me**: Get current user profile
- **PUT /api/users/{id}**: Update user (admin only)
- **PUT /api/users/me**: Update current user profile
- **POST /api/users/change-password**: Change password
- **DELETE /api/users/{id}**: Delete user (admin only)

### Dashboard

- **GET /api/dashboard**: Get dashboard data
- **GET /api/dashboard/stats**: Get summary statistics

## Flutter Integration

Please refer to the README-FLUTTER.md file for detailed instructions on integrating the backend with a Flutter frontend.

## Security

The application uses JWT-based authentication for secure API access. All API endpoints (except for authentication endpoints) require a valid JWT token in the Authorization header.

## Next Steps

1. Implement the Flutter frontend as described in README-FLUTTER.md
2. Deploy the backend to a production environment
3. Configure the Flutter app to use the production backend URL
4. Build and release the Flutter app for Android and iOS