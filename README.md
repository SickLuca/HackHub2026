# 🏆 HackHub2026

*HackHub2026* is a RESTful backend platform for comprehensive hackathon management, developed as a project for the *Software Engineering (IDS)* course at the *University of Camerino (UNICAM)*.

The system allows organizers, mentors, and participants to manage the entire lifecycle of a hackathon: from event creation to winner announcement, including team management, submissions, evaluations, reports, and support requests.

---

## 📋 Table of Contents

- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Running the Application](#-running-the-application)
- [Technology Stack](#-technology-stack)
- [Project Architecture](#-project-architecture)
- [API Documentation (Swagger)](#-api-documentation-swagger)

---

## ⚙️ Prerequisites

Before you begin, make sure you have the following installed:

| Requirement | Minimum Version | Notes |
|---|---|---|
| *Java JDK* | 21+ | Required for Spring Boot 4.x |
| *Git* | any | To clone the repository |


> *Note:* There is no need to install Gradle. The project includes the *Gradle Wrapper* (gradlew / gradlew.bat) which automatically downloads the correct version (Gradle 9.2.0).

---

## 📥 Installation

### 1. Clone the repository


git clone https://github.com/SickLuca/HackHub2026.git



### 2. Verify the Java version


java -version


Make sure the output shows version *21* or higher.

---

## 🚀 Running the Application

### On Windows


.\gradlew.bat bootRun


### On macOS / Linux


./gradlew bootRun


Once started, the application will be accessible at:

| Service | URL |
|---|---|
| *Application* | http://localhost:8080 |
| *Swagger UI* | http://localhost:8080/swagger-ui/index.html |

---

## 🛠️ Technology Stack

### Framework & Runtime

| Technology | Version | Description |
|---|---|---|
| *Spring Boot* | 4.0.1 | Main framework for building enterprise-ready Java applications. Handles auto-configuration, dependency injection (IoC), and embedded server (Tomcat). |
| *Spring Web (MVC)* | — | Module for building REST APIs via annotations (@RestController, @GetMapping, etc.). |
| *Spring Data JPA* | — | Abstracts database access through Repository interfaces, eliminating the need to write manual SQL queries for CRUD operations. |
| *Spring Security* | — | Framework for authentication and authorization. In this project it manages access via JWT tokens. |
| *Spring Validation* | — | Provides validation annotations (@NotBlank, @Min, @Email, etc.) to automatically validate incoming DTOs at the API layer. |
| *Spring Scheduling* | — | Enables the execution of scheduled tasks (@Scheduled) for automatic operations such as closing expired hackathons. |
| *Gradle* | 9.2.0 | Build tool for compilation, dependency management, and project packaging. |
| *Java* | 21+ | Programming language and runtime. |

### Database

| Technology | Description |
|---|---|
| *H2 Database* | In-memory relational database, ideal for development and testing. Data is recreated on every application startup via a DatabaseSeeder. |
| *Hibernate (JPA)* | ORM (Object-Relational Mapping) that maps Java classes annotated with @Entity to database tables. Configured in ddl-auto=update mode. |

### Security

| Technology | Version | Description |
|---|---|---|
| *JJWT (JSON Web Token)* | 0.11.5 | Library for generating, signing, and validating JWT tokens. Used for stateless API authentication. Includes the jjwt-api, jjwt-impl, and jjwt-jackson modules. |

### Utility Libraries

| Technology | Description |
|---|---|
| *Lombok* | Compile-time code-generation library. Reduces boilerplate by automatically generating getters, setters, constructors, equals, hashCode, and toString via annotations such as @Data, @Getter, @AllArgsConstructor, @Builder, etc. |
| *SpringDoc OpenAPI* (v2.7.0) | Automatically generates OpenAPI 3.0 documentation from Spring controller annotations and exposes it through an interactive web interface (Swagger UI). |

### Testing

| Technology | Description |
|---|---|
| *Spring Boot Starter Test* | Module that includes all dependencies needed for testing: *JUnit 5* (test framework), *Mockito* (mocking), *AssertJ* (fluent assertions), and *Spring Test* (Spring test context). |

---

## 🏗️ Project Architecture

The project follows a *layered architecture* typical of Spring Boot applications:

```text
src/main/java/it/unicam/cs/ids/
│
├── Main.java                      # Application entry point
│
├── config/                        # Application configuration
│   ├── DatabaseSeeder.java        # Populates the DB with sample data on startup
│   └── OpenApiConfig.java         # Swagger/OpenAPI configuration with JWT
│
├── controllers/                   # Presentation layer (REST API)
│   ├── AuthController.java        # Registration and login
│   ├── HackathonController.java   # Hackathon CRUD and management
│   ├── InvitationController.java  # Team invitation management
│   ├── ReportController.java       # Reports and flagging
│   ├── SubmissionController.java   # Submission upload and evaluation
│   ├── SupportRequestController.java # Support requests
│   └── TeamController.java        # Team management
│
├── dtos/                          # Data Transfer Objects
│   ├── requests/                  # DTOs for incoming requests (17 DTOs)
│   └── responses/                 # DTOs for outgoing responses (8 DTOs)
│
├── exceptions/                    # Centralized error handling
│   ├── GlobalExceptionHandler.java    # Global handler (@RestControllerAdvice)
│   ├── ApiErrorResponseDTO.java       # Standard error response format
│   ├── ResourceNotFoundException.java # 404 - Resource not found
│   ├── RuleViolationException.java    # 409 - Business rule violation
│   ├── InvalidInputException.java     # 400 - Invalid input
│   └── UnauthorizedActionException.java # 401 - Unauthorized action
│
├── models/                        # JPA entities (domain)
│   ├── abstractions/              # Abstract User class
│   ├── utils/                     # Enums (roles, states, payment methods)
│   ├── DefaultUser.java           # Standard user (participant)
│   ├── StaffUser.java             # Staff user (organizer/mentor)
│   ├── Hackathon.java             # Hackathon entity
│   ├── Team.java                  # Team entity
│   ├── Submission.java            # Submission entity
│   ├── Invitation.java            # Invitation entity
│   ├── Report.java                # Report/flag entity
│   └── SupportRequest.java        # Support request entity
│
├── repositories/                  # Data access layer (Spring Data JPA)
│   └── I*Repository.java          # 9 repository interfaces (one per entity)
│
├── security/                      # Security configuration and JWT
│   ├── SecurityConfig.java        # HTTP filter and permission configuration
│   ├── JwtAuthenticationFilter.java # Filter for JWT token validation
│   ├── JwtService.java            # JWT token generation and parsing
│   ├── CustomUserDetails.java     # Spring Security UserDetails implementation
│   ├── CustomUserDetailsService.java # Loads user from DB for authentication
│   └── SecurityUtils.java         # Utility to access the authenticated user
│
├── services/                      # Business logic layer
│   ├── abstractions/              # Service interfaces (contracts)
│   ├── AuthenticationService.java # Registration/login logic
│   ├── HackathonService.java      # Hackathon management logic
│   ├── InvitationService.java     # Invitation management logic
│   ├── ReportService.java         # Report management logic
│   ├── SubmissionService.java     # Submission management logic
│   ├── SupportRequestService.java # Support request logic
│   └── TeamService.java           # Team management logic
│
└── utils/                         # Design patterns and utilities
    ├── adapter/                   # Adapter Pattern (calendar integration)
    ├── builder/                   # Builder Pattern (hackathon creation)
    ├── strategy/                  # Strategy Pattern (payment methods)
    ├── scheduler/                 # Automatic schedulers
    └── unitOfWork/                # Unit of Work Pattern (transactions)
```

---

## 📖 API Documentation (Swagger)

With the application running, access the interactive API documentation at:


http://localhost:8080/swagger-ui/index.html


### Authentication on Swagger

The APIs are protected by *JWT*. To make authenticated calls:

1. Call the *login* endpoint (/api/auth/login) with your credentials
2. Copy the JWT token from the response
3. Click the *"Authorize"* 🔓 button at the top right of the Swagger UI
4. Enter the token in the format: Bearer <your-token>
5. All subsequent calls will automatically include the authentication header

---
