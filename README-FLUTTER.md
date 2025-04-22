# Blood Pressure Tracker - Flutter Integration Guide

This document provides instructions for integrating the Blood Pressure Tracker backend with a Flutter frontend.

## Overview of Changes

The following changes have been made to the application to support a Flutter frontend:

1. **REST API Endpoints**: Created REST API controllers for all major resources (patients, blood pressure readings, users, dashboard)
2. **JWT Authentication**: Implemented JWT-based authentication for secure API access
3. **CORS Configuration**: Added CORS support to allow cross-origin requests from the Flutter app

## API Endpoints

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

### 1. Set Up Flutter Project

Create a new Flutter project:

```bash
flutter create bp_tracker_app
cd bp_tracker_app
```

### 2. Add Required Dependencies

Add the following dependencies to your `pubspec.yaml`:

```yaml
dependencies:
  flutter:
    sdk: flutter
  http: ^1.1.0
  shared_preferences: ^2.2.0
  provider: ^6.0.5
  intl: ^0.18.1
  fl_chart: ^0.63.0
```

Run `flutter pub get` to install the dependencies.

### 3. Create API Service

Create a service to handle API requests:

```dart
// lib/services/api_service.dart
import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

class ApiService {
  final String baseUrl = 'http://your-backend-url/api';
  
  Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('token');
  }
  
  Future<void> saveToken(String token) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('token', token);
  }
  
  Future<Map<String, String>> getHeaders() async {
    final token = await getToken();
    return {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer $token',
    };
  }
  
  Future<dynamic> login(String username, String password) async {
    final response = await http.post(
      Uri.parse('$baseUrl/auth/login'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'username': username,
        'password': password,
      }),
    );
    
    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      await saveToken(data['token']);
      return data;
    } else {
      throw Exception('Failed to login');
    }
  }
  
  Future<dynamic> getPatients() async {
    final headers = await getHeaders();
    final response = await http.get(
      Uri.parse('$baseUrl/patients'),
      headers: headers,
    );
    
    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception('Failed to get patients');
    }
  }
  
  // Add more methods for other API endpoints
}
```

### 4. Create Models

Create models for your data:

```dart
// lib/models/patient.dart
class Patient {
  final int id;
  final String firstName;
  final String lastName;
  final String dateOfBirth;
  final String gender;
  final String phoneNumber;
  final String email;
  final String address;
  final String medicalHistory;
  final String registrationDate;
  
  Patient({
    required this.id,
    required this.firstName,
    required this.lastName,
    required this.dateOfBirth,
    required this.gender,
    required this.phoneNumber,
    required this.email,
    required this.address,
    required this.medicalHistory,
    required this.registrationDate,
  });
  
  factory Patient.fromJson(Map<String, dynamic> json) {
    return Patient(
      id: json['id'],
      firstName: json['firstName'],
      lastName: json['lastName'],
      dateOfBirth: json['dateOfBirth'],
      gender: json['gender'],
      phoneNumber: json['phoneNumber'] ?? '',
      email: json['email'] ?? '',
      address: json['address'] ?? '',
      medicalHistory: json['medicalHistory'] ?? '',
      registrationDate: json['registrationDate'],
    );
  }
}

// Create similar models for BloodPressureReading, User, etc.
```

### 5. Create Screens

Create screens for your app:

```dart
// lib/screens/login_screen.dart
import 'package:flutter/material.dart';
import '../services/api_service.dart';

class LoginScreen extends StatefulWidget {
  @override
  _LoginScreenState createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _formKey = GlobalKey<FormState>();
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();
  final _apiService = ApiService();
  bool _isLoading = false;
  
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Login')),
      body: Padding(
        padding: EdgeInsets.all(16.0),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              TextFormField(
                controller: _usernameController,
                decoration: InputDecoration(labelText: 'Username'),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Please enter your username';
                  }
                  return null;
                },
              ),
              TextFormField(
                controller: _passwordController,
                decoration: InputDecoration(labelText: 'Password'),
                obscureText: true,
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Please enter your password';
                  }
                  return null;
                },
              ),
              SizedBox(height: 20),
              _isLoading
                  ? CircularProgressIndicator()
                  : ElevatedButton(
                      onPressed: _login,
                      child: Text('Login'),
                    ),
            ],
          ),
        ),
      ),
    );
  }
  
  Future<void> _login() async {
    if (_formKey.currentState!.validate()) {
      setState(() {
        _isLoading = true;
      });
      
      try {
        await _apiService.login(
          _usernameController.text,
          _passwordController.text,
        );
        Navigator.pushReplacementNamed(context, '/dashboard');
      } catch (e) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Login failed: $e')),
        );
      } finally {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }
}

// Create similar screens for dashboard, patient list, patient details, etc.
```

### 6. Set Up Navigation

Set up navigation in your app:

```dart
// lib/main.dart
import 'package:flutter/material.dart';
import 'screens/login_screen.dart';
import 'screens/dashboard_screen.dart';
import 'screens/patient_list_screen.dart';
import 'screens/patient_details_screen.dart';
// Import other screens

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'BP Tracker',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      initialRoute: '/login',
      routes: {
        '/login': (context) => LoginScreen(),
        '/dashboard': (context) => DashboardScreen(),
        '/patients': (context) => PatientListScreen(),
        '/patient_details': (context) => PatientDetailsScreen(),
        // Add routes for other screens
      },
    );
  }
}
```

## Security Considerations

1. **HTTPS**: Ensure your backend uses HTTPS in production
2. **Token Storage**: Store JWT tokens securely using secure storage
3. **Token Expiration**: Handle token expiration and refresh tokens
4. **Input Validation**: Validate all user input on both client and server
5. **Error Handling**: Implement proper error handling and don't expose sensitive information

## Next Steps

1. Implement all screens and functionality in your Flutter app
2. Test the integration thoroughly
3. Deploy your backend to a production environment
4. Configure your Flutter app to use the production backend URL
5. Build and release your Flutter app for Android and iOS