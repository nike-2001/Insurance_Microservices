# Microservices Insurance Product Platform

## Overview
This project is a microservices-based product platform designed to handle various functionalities such as policy management, product catalog, claim processing, configuration management, and service discovery. The application leverages modern technologies like Spring Cloud, Docker, Kubernetes, and Jenkins for a scalable and robust architecture. Authentication is implemented using Auth0 for secure user access.

The project is divided into multiple independent modules, each responsible for a specific functionality, communicating via REST APIs and following a microservices architecture pattern. Unit tests ensure code reliability, and CI/CD pipelines are set up using Jenkins for automated builds and deployments. Docker and Kubernetes are used for containerization and orchestration, respectively.

## Architecture
The platform consists of the following microservices:

### 1. Policy Service
- **Port**: 8081
- **Description**: Manages policy-related operations, such as creating, updating, and retrieving policies.
- **Key Features**:
  - Policy creation and management
  - Communicates with the Product and Claim services for policy processing.
  - Integration with payment processing

### 2. Product Service
- **Port**: 8082
- **Description**: Manages the product catalog, including product details and coverage information.
- **Key Features**:
  - Provides CRUD operations for products.
  - Product validation and availability checks

### 3. Claim Service
- **Port**: 8083
- **Description**: Handles claim processing and management.

### 4. Config Service
- **Description**: Centralizes configuration management for all microservices.
- **Responsibilities**:
  - Stores and serves configuration properties for other services.
  - Enables dynamic configuration updates without restarting services.
- **Technology**: Spring Cloud Config

### 5. Service Registry
- **Description**: Acts as a service discovery mechanism to allow microservices to locate each other dynamically.
- **Responsibilities**:
  - Registers and deregisters services.
  - Provides load balancing and service lookup.
- **Technology**: Eureka (Spring Cloud Netflix)

## Authentication
- **Provider**: Auth0
- **Description**: Implements secure authentication and authorization for the application.
- **Features**:
  - JWT-based authentication for securing APIs.
  - Role-based access control for different user types (Customer, Agent, Admin).
  - Integration with the Cloud Gateway for validating requests.

## Technologies Used
- **Backend**: Spring Boot, Spring Cloud (Gateway, Config, Eureka)
- **Authentication**: Auth0
- **Testing**: JUnit, Mockito (for unit testing)
- **Containerization**: Docker
- **Orchestration**: Kubernetes
- **CI/CD**: Jenkins
- **Database**: MySQL, H2
- **Other**: REST APIs, Maven (build tool)

## Setup and Installation
### Prerequisites
- Java 17 or higher
- Maven/Gradle (depending on your build tool)
- Docker and Docker Compose
- Kubernetes (Minikube or a cloud provider like GKE, EKS, etc.)
- Jenkins (for CI/CD pipeline)
- Auth0 account for authentication setup
