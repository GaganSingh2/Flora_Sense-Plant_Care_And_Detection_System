# Flora_Sense-Plant_Care_And_Detection_System

# 🌿FloraSense — Plant Care & Detection System (Backend)

A Spring Boot REST API for plant identification, plant information management, and personalized plant care.

FloraSense is a backend application designed to help users identify plants from images and access structured information about their characteristics, benefits, toxicity, pet safety, and growing requirements.

The backend integrates the Pl@ntNet Plant Identification API to identify plant species from uploaded images. It uses Spring Boot to expose REST APIs, Spring Security and JWT for authentication, Spring Data JPA and Hibernate for database operations, and PostgreSQL for persistent data storage.

The backend also provides APIs for user and administrator management, plant exploration, favorites, identification history, dashboards, trending plants, and CSV exports.

Project status: Backend development is implemented for the documented Version 1 modules. Frontend development using React.js is planned and has not yet been completed.

# 🌱 Project Overview

People often purchase or grow plants without knowing enough about their benefits, toxicity, pet safety, medicinal uses, or growing requirements. Identifying an unknown plant and finding reliable information about it can also be time-consuming.

FloraSense addresses this problem by combining image-based plant identification with a structured plant information database.

The backend accepts plant image uploads, communicates with Pl@ntNet to identify likely species, processes identification results, and stores upload records in PostgreSQL. It provides APIs to retrieve plant information, manage identification history, maintain favorites, and retrieve dashboard statistics.

Administrators can manage user accounts and plant records, monitor application statistics, and export data in CSV format.

The backend is being developed as the foundation for a React-based frontend.

# 🎯 Problem Statement

Plant enthusiasts and buyers may lack convenient access to organized information about the plants they purchase or grow. Relying only on seller recommendations or scattered online sources can make it difficult to understand a plant's characteristics, care requirements, and potential safety risks.

There is a need for a centralized application backend that identifies plants from images and provides structured, accessible plant information through REST APIs.

FloraSense addresses this need by integrating an external plant identification service with a relational database and APIs for plant discovery, user activity, and administrative management.

# 🎯 Project Objectives

  1. Provide plant identification using uploaded images and the Pl@ntNet API.
  2. Store plant information in a structured PostgreSQL database.
  3. Provide APIs for plant details, search, filtering, sorting, and pagination.
  4. Implement user registration, login, and JWT-based authentication.
  5. Maintain plant identification history and favorite plant records.
  6. Provide personalized user dashboard statistics and recent discoveries.
  7. Provide trending and featured plant information based on identification activity.
  8. Develop administrative APIs for managing users and plant records.
  9. Support CSV exports for user data, plant information, and identification history.
  10. Build a maintainable backend architecture that can be integrated with a frontend application.

# ✨ Key Backend Features

  1. RESTful API development using Spring Boot.
  2. User registration and login.
  3. JWT authentication for protected endpoints.
  4. BCrypt password hashing.
  5. Role-based access control for user and administrator operations.
  6. Plant image upload and external identification through Pl@ntNet.
  7. Plant information retrieval and management.
  8. Search, filtering, sorting, and pagination in supported endpoints.
  9. Favorite plant management.
  10. Plant identification history and record deletion.
  11. Conditional deletion of image files when no other upload records reference them.
  12. User and administrator dashboard statistics.
  13. Trending and featured plant APIs.
  14. CSV exports for supported data modules.
  15. Soft deletion and restoration of plant records.
  16. Centralized validation and exception handling.

# 🏗️ System Architecture
FloraSense follows a layered backend architecture. REST controllers receive client requests, service classes implement business logic, repositories manage database operations, and PostgreSQL stores application data.

The plant identification service communicates with Pl@ntNet when processing uploaded images.
             API Client / Postman
                       
                       | HTTP Requests
                       v
             +----------------------+
             | Spring Boot REST API |
             |                      |
             | Controllers          |
             +----------+-----------+
                        |
                        v
             +----------------------+
             | Service Layer        |
             |                      |
             | Business Logic       |
             | Validation           |
             | Plant Identification |
             +-----+-----------+----+
                   |           |
                   v           v
          +----------------+  +----------------+
          | Spring Data    |  | Pl@ntNet API   |
          | JPA / Hibernate|  |                |
          +-------+--------+  +----------------+
                  |
                  v
          +----------------+
          | PostgreSQL     |
          |                |
          | Users          |
          | Plant Info     |
          | Plant Uploads  |
          | Favorites      |
          +----------------+

Layer responsibilities
- Controller layer: Exposes REST endpoints and handles HTTP requests and responses.
- Service layer: Implements application rules, coordinates database operations, and integrates with Pl@ntNet.
- Repository layer: Provides database access through Spring Data JPA.
- Entity layer: Maps Java objects to PostgreSQL tables.
- Security layer: Applies authentication and authorization rules.
- External integration: Communicates with Pl@ntNet for image-based plant identification.

# 🗄️ Database Design
FloraSense uses PostgreSQL as its relational database.
<!-- <img width="892" height="731" alt="Screenshot 2026-10-25 030947" src="https://github.com/user-attachments/assets/f82a59b8-cea0-4ff0-a403-c6721806c6ab" /> -->
<img width="915" height="610" alt="Plant_Care & Detection System" src="https://github.com/user-attachments/assets/d34ab351-dbf7-4c54-a98f-ef65e6d62e99" />

# 🛠️ Technology Stack
<img width="945" height="716" alt="image" src="https://github.com/user-attachments/assets/ddaca8eb-c175-41e7-b0fe-bf76f993d5ee" />
