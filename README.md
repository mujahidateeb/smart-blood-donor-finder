# Smart Blood Donor Finder

A Java 11 / Spring Boot 2.7 / Maven application that helps patients find potential blood donors by blood group and location.

## Features
- Donor and patient registration/login
- Role-based donor and patient dashboards
- Donor profile and availability management
- Search donors by blood group, city and area
- Create emergency blood requests
- Donors can accept or reject requests
- Patients can track request status
- Contact information is not exposed in public donor search results
- Admin role support at the security/model level

## Requirements
- JDK 11
- Maven 3.6+
- MySQL 8+

## Setup
1. Create the database:
   ```sql
   CREATE DATABASE smart_blood_donor;
   ```
2. Update `src/main/resources/application.properties` with your MySQL username/password.
3. Build:
   ```bash
   mvn clean package
   ```
4. Run:
   ```bash
   mvn spring-boot:run
   ```
5. Open `http://localhost:8080`.

## First user / admin
Register through `/register`. The public registration page allows DONOR and PATIENT roles only.
For local development, create an admin account manually by changing the role in MySQL after registering:
```sql
UPDATE users SET role='ADMIN' WHERE email='admin@example.com';
```

## Main routes
- `/` – home
- `/register` – registration
- `/login` – login
- `/donors` – donor search (login required)
- `/requests/new` – create blood request (PATIENT)
- `/dashboard` – role-aware dashboard
- `/profile` – view and update user profile / donation availability / password
- `/requests/{id}` – request details/status (request owner or assigned donor)

## Matching notes
The initial matching logic uses exact blood-group match plus case-insensitive city match and optional area filtering. This is a practical first version, not a clinical compatibility engine. Always confirm blood compatibility and eligibility with qualified medical staff/blood banks. Donors listed by the app are potential contacts, not guaranteed available donors.

## Security notes before production
- Configure HTTPS, secure session cookies and CSRF protections (enabled by default).
- Add email/phone OTP verification, rate limiting, CAPTCHA and audit logging.
- Add consent and privacy/retention policies before collecting personal data.
- Do not publish donor phone/email in search results.
- Review local laws and healthcare/privacy requirements.
- Add pagination, monitoring, backups and database migrations (Flyway/Liquibase).
