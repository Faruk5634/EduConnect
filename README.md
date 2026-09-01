# 🎓 EduConnect

[![Build backend](https://github.com/Faruk5634/EduConnect/actions/workflows/maven.yml/badge.svg)](https://github.com/Faruk5634/EduConnect/actions)
[![Frontend build](https://github.com/Faruk5634/EduConnect/actions/workflows/frontend.yml/badge.svg)](https://github.com/Faruk5634/EduConnect/actions)
[![GitHub Pages](https://github.com/Faruk5634/EduConnect/actions/workflows/gh-pages.yml/badge.svg)](https://github.com/Faruk5634/EduConnect/actions)

EduConnect is an enterprise-ready, multi-tenant School Management System (SaaS) with a Spring Boot backend and a React + TypeScript + Tailwind frontend. It provides role-based access (Super Admin, School Admin, Teacher, Student, Parent) and is designed with SOLID principles, tenant isolation, and testability in mind.

Live demo (when deployed): https://Faruk5634.github.io/EduConnect

## 📦 Repository structure

- backend/: Spring Boot application (source in src/main/java)
- frontend/: React + Vite + TypeScript frontend
- docs/: Optional documentation and architecture diagrams

## 🏗️ Architecture & Design
EduConnect follows a layered and modular architecture:

- Controller (REST API) — thin layers that delegate to services
- Service — business logic, tenant guards, and orchestration
- Repository — Spring Data JPA repositories with tenant-scoped queries
- Mapper — DTO <-> entity mappers to keep services clean (MapStruct or static mappers)
- Frontend — reusable UI primitives and custom hooks (useProfile, useAnnouncements, etc.)

Key design goals: SOLID, DRY, tenant isolation, and small testable units.

## 🚀 Quick start (development)

Prerequisites:
- Java 17+
- Maven
- Node 18+
- PostgreSQL (or other DB configured in application.properties)

Backend:

1. Configure application properties (src/main/resources/application.yml or env vars):
   - spring.datasource.url, username, password
   - jwt.secret (if used)
   - app.multiTenant.enabled (if any)

2. Build and run backend:

```bash
# from repo root
./mvnw -DskipTests package
java -jar target/educonnect-0.0.1-SNAPSHOT.jar
```

Frontend:

```bash
cd frontend
npm ci
npm run dev    # development
npm run build  # production build
```

Deployment (GitHub Pages): the repo includes a workflow to build the frontend and publish the `frontend/dist` to the `gh-pages` branch.

## 🛡️ Multi-tenant & Security Notes

- Services enforce tenant (school) guards to avoid cross-tenant data leakage.
- ROLE_SUPER_ADMIN is treated as a global actor; tenant-scoped roles are enforced for school admins and users.
- Sensitive configuration (DB, JWT secrets) must be supplied via secure environment variables in production.

## ✅ Contributing

Please follow the guidelines:
- Open an issue before large changes
- Use feature branches and create PRs targeting `main`
- Run linters and tests locally before opening PRs
- Keep changes small and focused (one concern per PR)

## ⚙️ CI / CD

- Backend builds via Maven workflows (maven.yml)
- Frontend builds via a frontend workflow (frontend.yml)
- Pages deployment via .github/workflows/gh-pages.yml (this workflow publishes frontend/dist)

## 📄 License

MIT © Faruk5634

---

If you want, the README can include step-by-step environment examples, example curl requests, and architecture diagrams. Tell me which details to expand.