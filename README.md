# BFS-Forum

BFS-Forum is a multi-module Spring Boot application designed to provide a robust and scalable forum platform. This project leverages Spring Cloud for microservices architecture, including a Eureka Discovery Service and an API Gateway, to enable distributed components.

---

## Project Structure

This project is structured as a Maven multi-module project. The `pom.xml` indicates the following modules are either active or planned:

* **`eureka`**: Service Discovery with Spring Cloud Eureka. This module acts as a central registry for all microservices.
* **`api-gateway`**: An API Gateway responsible for routing requests to various microservices, handling cross-cutting concerns like security, monitoring, and resiliency.
* **`auth-service`**: Handles user authentication and authorization logic.
* **`user-service`** (Planned/Commented Out): Likely for managing user profiles and related operations.
* **`history-service`** (Planned/Commented Out): Could be for tracking user activity or forum history.
* **`post-service`** (Planned/Commented Out): For managing forum posts, threads, and comments.
* **`message-service`** (Planned/Commented Out): Potentially for direct messaging or notifications within the forum.
* **`file-service`** (Planned/Commented Out): For handling file uploads and storage related to forum content.
* **`email-service`** (Planned/Commented Out): For sending email notifications (e.g., password resets, new messages).

---

## Technologies Used

* **Java 17**: The primary programming language for the application.
* **Spring Boot 3.5.0**: Provides the foundational framework for building production-ready Spring applications.
* **Spring Cloud 2024.0.1**: Enables microservices patterns such as service discovery and API Gateway.
* **Lombok**: Reduces boilerplate code for Java classes (e.g., getters, setters, constructors).
* **MongoDB**: Expected to be used as the unrelational database for posts.
* **MySQL**: (Indicated by `mysql.version` in `pom.xml`) Expected to be used as the relational database for everything else.
* **Hibernate 6.4.4.Final**: (Indicated by `hibernate.version` in `pom.xml`) Likely used as the JPA implementation for database interaction.
* **Maven**: The build automation tool.

