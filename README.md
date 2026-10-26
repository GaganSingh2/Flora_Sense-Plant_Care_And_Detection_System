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

RESTful API development using Spring Boot.

User registration and login.

JWT authentication for protected endpoints.

BCrypt password hashing.

Role-based access control for user and administrator operations.

Plant image upload and external identification through Pl@ntNet.

Plant information retrieval and management.

Search, filtering, sorting, and pagination in supported endpoints.

Favorite plant management.

Plant identification history and record deletion.

Conditional deletion of image files when no other upload records reference them.

User and administrator dashboard statistics.

Trending and featured plant APIs.

CSV exports for supported data modules.

Soft deletion and restoration of plant records.

Centralized validation and exception handling.  
