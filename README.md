# 🎓 EduConnect - School Management System API

EduConnect is a comprehensive RESTful API built with Spring Boot, designed to streamline and manage school operations. It provides a robust backend architecture for handling students, teachers, parents, classrooms, and announcements.

## 🚀 Technologies Used
* **Java 17**
* **Spring Boot 3.x**
* **Spring Data JPA / Hibernate**
* **PostgreSQL** (Relational Database)
* **Maven** (Dependency Management)

## 🏗️ Architecture
This project follows a professional **Layered Architecture** pattern:
* **Controller Layer:** Handles incoming HTTP requests and routes.
* **Service Layer:** Contains the core business logic.
* **Repository Layer:** Manages database interactions using Spring Data JPA.
* **Model (Entity) Layer:** Represents database tables and relationships (OneToMany, ManyToOne).

## 📌 Features
* CRUD operations for Students, Teachers, and Parents.
* Classroom management (assigning students and homeroom teachers).
* Announcement system with automated timestamps.
* Relational data mapping.