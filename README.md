# Microservices E-Commerce Platform

## Project Overview
This project is a microservices-based e-commerce platform designed to handle various functionalities such as order management, product catalog, payment processing, configuration management, and service discovery. The application leverages modern technologies like Spring Cloud, Docker, Kubernetes, and Jenkins for a scalable and robust architecture. Authentication is implemented using Auth0 for secure user access.

The project is divided into multiple independent modules, each responsible for a specific functionality, communicating via REST APIs and following a microservices architecture pattern. Unit tests ensure code reliability, and CI/CD pipelines are set up using Jenkins for automated builds and deployments. Docker and Kubernetes are used for containerization and orchestration, respectively.

## Modules
The project consists of the following modules:

### 1. Cloud Gateway
- **Description**: Acts as the entry point for all client requests, providing routing, load balancing, and API gateway functionalities.
- **Responsibilities**:
  - Routes requests to appropriate microservices based on URL patterns.
  - Handles cross-cutting concerns like authentication, logging, and monitoring.
- **Technology**: Spring Cloud Gateway

### 2. Order Service
- **Description**: Manages order-related operations, such as creating, updating, and retrieving orders.
- **Responsibilities**:
  - Handles order creation, status updates, and order history.
  - Communicates with the Product and Payment services for order processing.
- **Technology**: Spring Boot, REST APIs

### 3. Product Service
- **Description**: Manages the product catalog, including product details and inventory.
- **Responsibilities**:
  - Provides CRUD operations for products.
  - Maintains product availability and details.
- **Technology**: Spring Boot, REST APIs

### 4. Payment Service
- **Description**: Handles payment processing for orders.
- **Responsibilities**:
  - Processes payments and updates order status.
  - Integrates with external payment gateways (simulated in this project).
- **Technology**: Spring Boot, REST APIs

### 5. Config Service
- **Description**: Centralizes configuration management for all microservices.
- **Responsibilities**:
  - Stores and serves configuration properties for other services.
  - Enables dynamic configuration updates without restarting services.
- **Technology**: Spring Cloud Config

### 6. Service Registry
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
  - Role-based access control for different user types.
  - Integration with the Cloud Gateway for validating requests.

## Technologies Used
- **Backend**: Spring Boot, Spring Cloud (Gateway, Config, Eureka)
- **Authentication**: Auth0
- **Weighting**: JUnit, Mockito (for unit testing)
- **Containerization**: Docker
- **Orchestration**: Kubernetes
- **CI/CD**: Jenkins
- **Database**: MySQL
- **Other**: REST APIs, Maven (build tool)

## Setup and Installation
### Prerequisites
- Java 17 or higher
- Maven/Gradle (depending on your build tool)
- Docker and Docker Compose
- Kubernetes (Minikube or a cloud provider like GKE, EKS, etc.)
- Jenkins (for CI/CD pipeline)
- Auth0 account for authentication setup

