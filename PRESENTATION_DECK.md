# Presentation Slide Deck: Smart Blood Donor Finder
**An Intelligent, Privacy-Centric Blood Donation and Emergency Matching Platform**

*A clean, structured, and easy-to-understand slide deck designed for academic presentations, client walkthroughs, and technical project defense.*

---

## Slide Navigation & Complete 26-Slide Structure

> [!TIP]
> **How to Run the Presentation**:
> - **Direct Full-Screen Slide Show**: Double-click `PRESENTATION_DECK.ppsx`. It launches directly into presentation mode with single-click zoom enabled.
> - **Standard PowerPoint**: Open `PRESENTATION_DECK.pptx` and press **F5** to start the slideshow.
> - **In Edit Mode**: Every feature has its Full-Screen HD screenshot right on the very next slide (Slides 8, 10, 12, 14, 16, 18, 20).

| Slide # | Slide Title | Category | Visual Asset / Screenshot |
| :---: | :--- | :--- | :--- |
| **Slide 1** | Title Slide: Smart Blood Donor Finder | Overview | Brand Hero Graphic & Tech Badges |
| **Slide 2** | The Problem: Why Traditional Systems Fail | Problem Statement | Real-World Consequence Analysis |
| **Slide 3** | The Solution: Smart Blood Donor Finder | Innovation | 3 Core Pillars (Geo, Privacy, Roles) |
| **Slide 4** | Complete Technology Stack | Engineering | Full-Stack Grid (Java 11, Spring Boot, MySQL) |
| **Slide 5** | System Architecture & MVC Data Flow | Architecture | Tiered MVC & Security Filter Flow |
| **Slide 6** | User Roles & Access Control Model | Security | Donor vs. Patient vs. Admin Matrix |
| **Slide 7** | Feature 1: Registration & Flexible Roles | Core Feature | `02_registration_page_filled.png` (Preview) |
| **Slide 8** | 🔍 **Full-Screen HD View: User Registration** | **Maximized UI** | **`02_registration_page_filled.png` (Full HD)** |
| **Slide 9** | Feature 2: Role-Aware Dashboard (Donor View) | Core Feature | `04_donor_dashboard.png` (Preview) |
| **Slide 10** | 🔍 **Full-Screen HD View: Donor Portal Dashboard** | **Maximized UI** | **`04_donor_dashboard.png` (Full HD)** |
| **Slide 11** | Feature 3: Role-Aware Dashboard (Patient View) | Core Feature | `05_patient_dashboard.png` (Preview) |
| **Slide 12** | 🔍 **Full-Screen HD View: Patient Portal Dashboard** | **Maximized UI** | **`05_patient_dashboard.png` (Full HD)** |
| **Slide 13** | Feature 4: Privacy-First Donor Directory | Core Feature | `07_donor_search_directory.png` (Preview) |
| **Slide 14** | 🔍 **Full-Screen HD View: Donor Directory Search** | **Maximized UI** | **`07_donor_search_directory.png` (Full HD)** |
| **Slide 15** | Feature 5: Emergency Blood Request Form | Core Feature | `06_emergency_request_form.png` (Preview) |
| **Slide 16** | 🔍 **Full-Screen HD View: Emergency Request Form** | **Maximized UI** | **`06_emergency_request_form.png` (Full HD)** |
| **Slide 17** | Feature 6: Mutual Handshake & Contact Revelation | Core Feature | `09_request_detail_matched.png` (Preview) |
| **Slide 18** | 🔍 **Full-Screen HD View: Mutual Handshake Unlocked** | **Maximized UI** | **`09_request_detail_matched.png` (Full HD)** |
| **Slide 19** | Feature 7: Administration Console & Governance | Core Feature | `10_admin_users.png` (Preview) |
| **Slide 20** | 🔍 **Full-Screen HD View: Administration Console** | **Maximized UI** | **`10_admin_users.png` (Full HD)** |
| **Slide 21** | Database Design & Entity Relationships | Data Layer | Relational Schema (`users`, `user_roles`, `blood_requests`) |
| **Slide 22** | Project Architecture: Source Code & Packages | Architecture | Java Package Tree (`com.smartblood.*`) |
| **Slide 23** | Deployment & Setup: Step-by-Step Guide | Setup & Ops | 4 Setup Steps & Admin Promotion SQL |
| **Slide 24** | Quality Assurance, Testing & Security | Verification | JUnit 5 MockMvc Suite & Defense In Depth |
| **Slide 25** | Project Roadmap & Future Enhancements | Roadmap | Universal Compatibility, SMS Gateway, GPS |
| **Slide 26** | Conclusion & Academic Summary / Q&A | Conclusion | Handover Deliverables & Open Floor |

---

## Slide 1: Title Slide

### Visual Layout
- **Header**: Smart Blood Donor Finder
- **Subheading**: An Intelligent, Privacy-Preserving Blood Donation & Emergency Matching Platform
- **Tags**: Java 11 (LTS) • Spring Boot 2.7 • MySQL 8.0 • Spring Security 5 • Thymeleaf
- **Demonstration User**: Mariya Firdous (Channagiri, Karnataka)

### Presenter Notes / Speaker Script
> *"Respected evaluators and audience, today we present Smart Blood Donor Finder—a full-stack web application engineered to bridge the life-critical gap between patients needing emergency transfusions and compatible local donors, with strict privacy protection."*

---

## Slide 2: Problem Statement

### Key Bullet Points
- **Critical Time Delays**: Families resort to frantic social media posts and WhatsApp chains lacking geographic precision.
- **Donor Privacy Violations**: Public broadcast of personal phone numbers leads to harassment and donor burnout.
- **Fragmented Blood Bank Data**: Standalone blood banks lack real-time visibility into local volunteer donors in nearby municipalities like Channagiri.

### Presenter Notes / Speaker Script
> *"Traditional blood donation systems suffer from severe friction: slow emergency matching, public phone number leakage, and lack of real-time locality filtering. During trauma surgeries or obstetric emergencies in regional towns like Channagiri, every elapsed minute exponentially increases patient mortality."*

---

## Slide 3: The Solution

### Key Bullet Points
- **Precision Geo-Matching**: Instant filtering by exact blood group and municipality (Channagiri) connects patients directly to nearby willing donors without broadcast clutter.
- **Two-Way Privacy Handshake**: Donor phone numbers remain encrypted and masked. Contact information is unlocked ONLY when an eligible donor actively accepts an emergency request.
- **Multi-Role Single Account**: Users seamlessly switch between Donor and Patient portals within one unified profile without maintaining separate credentials.

---

## Slide 4: Complete Technology Stack

### Key Bullet Points
- **Backend Framework**: Java 11 LTS with Spring Boot 2.7.18 for enterprise-grade scalability.
- **Security Engine**: Spring Security 5 with `DaoAuthenticationProvider`, `BCryptPasswordEncoder` (10 rounds), and CSRF protection.
- **Data Persistence**: Spring Data JPA & Hibernate 5.6 backed by a MySQL 8.0 relational database.
- **Presentation Layer**: Thymeleaf 3 server-rendered dynamic templates with responsive CSS Grid and Flexbox.
- **Automated Testing**: JUnit 5 + MockMvc test suite with 100% pass rates.

---

## Slide 5: System Architecture & Data Flow

### Key Bullet Points
- **Layer 1: Presentation Layer**: Thymeleaf templates, CSS3, and dynamic tab switching.
- **Layer 2: Security Filter Chain**: Authenticates sessions, verifies CSRF tokens, and enforces RBAC matchers.
- **Layer 3: Controller Layer**: Dedicated Spring MVC controllers (`AuthController`, `DashboardController`, `DonorController`, `RequestController`, `AdminController`).
- **Layer 4: Service Layer**: Houses business logic, role-checking, geo-filtering, and request lifecycle validation.
- **Layer 5: Data Access Layer**: Spring Data JPA repositories interfacing with MySQL tables.

---

## Slide 6: User Roles & Access Control Model

### Key Capabilities Matrix
- **`ROLE_DONOR`**: Browse emergency requests strictly matching their blood group and registered city (or targeted to them), review requests, choose to Accept or Reject, toggle donation availability. Focused donor portal with redundant "Find other donors" navigation removed.
- **`ROLE_PATIENT`**: Publish emergency requests with hospital addresses, send direct requests to specific donors, update request details via form, track status (`OPEN`, `ACCEPTED`, `COMPLETED`, `CLOSED`), and close open requests with a single click.
- **`DUAL ROLE`**: Full access to both Donor and Patient portals under a single user profile.
- **`ROLE_ADMIN`**: Dedicated `/admin` console to manage registered accounts, assign/revoke roles, and moderate all emergency requests.

---

## Slide 7: Feature 1: Registration & Multi-Role Selection (Overview)

### Visual Layout
- Left: Architecture highlights (single-page onboarding, multi-role checkboxes, BCrypt encryption).
- Right: Preview screenshot `screenshots/02_registration_page_filled.png` with click-to-zoom button.
- Target: Jumps to Slide 8 for Full-Screen HD View.

---

## Slide 8: 🔍 [Full-Screen HD View] Feature 1: User Registration
- **High-Definition Screenshot**: Displays Mariya Firdous (`mariyafirdous@gmail.com`), blood group O+, Channagiri location, and dual checkboxes for Donor + Patient roles.
- **Interactive Controls**: Click image or `Next Feature →` to proceed to Slide 9; click `↩ Overview` to return to Slide 7.

---

## Slide 9: Feature 2: Role-Aware Dashboard (Donor View Overview)

### Visual Layout
- Left: Role chips header, donation availability switch, matched emergency feed (compatible blood group & city, or direct requests), "Accept request" and "Reject" actions. Redundant "Find other donors" button removed for a streamlined donor experience.
- Right: Preview screenshot `screenshots/04_donor_dashboard.png`.
- Target: Jumps to Slide 10 for Full-Screen HD View.

---

## Slide 10: 🔍 [Full-Screen HD View] Feature 2: Donor Portal Dashboard
- **High-Definition Screenshot**: Shows "Hello, Mariya Firdous", role badges (🩸 Donor, 🏥 Patient, ⚙️ Admin), "You are currently marked as: Available", and open Channagiri emergency transfusion requests matched to the donor with Accept and Reject actions.
- **Interactive Controls**: Click image to advance to Slide 11; click `↩ Overview` to return to Slide 9.

---

## Slide 11: Feature 3: Role-Aware Dashboard (Patient View Overview)

### Visual Layout
- Left: Emergency action bar, "+ New emergency request" quick-action, request tracking table, "Update" button to modify open requests via form, and one-click "Close request" button (`CLOSED` status).
- Right: Preview screenshot `screenshots/05_patient_dashboard.png`.
- Target: Jumps to Slide 12 for Full-Screen HD View.

---

## Slide 12: 🔍 [Full-Screen HD View] Feature 3: Patient Portal Dashboard
- **High-Definition Screenshot**: Patient portal tracking active requests for Channagiri Taluk General Hospital with status chip `ACCEPTED (Matched with donor!)` and action buttons to update or close requests.
- **Interactive Controls**: Click image to advance to Slide 13; click `↩ Overview` to return to Slide 11.

---

## Slide 13: Feature 4: Privacy-First Donor Directory (Overview)

### Visual Layout
- Left: Precision geo-search by blood group & city, phone number masking, anti-scraping design, and **"Send Request"** button allowing patients to send requests to target donors for their review and voluntary consent.
- Right: Preview screenshot `screenshots/07_donor_search_directory.png`.
- Target: Jumps to Slide 14 for Full-Screen HD View.

---

## Slide 14: 🔍 [Full-Screen HD View] Feature 4: Privacy-First Donor Directory
- **High-Definition Screenshot**: Directory search for O+ donors in Channagiri, highlighting the "Contact protected" badge shielding personal telephone numbers and the "Send Request" action button.
- **Interactive Controls**: Click image to advance to Slide 15; click `↩ Overview` to return to Slide 13.

---

## Slide 15: Feature 5: Emergency Blood Request Form (Overview)

### Visual Layout
- Left: Clinical specificity (blood group, units), hospital facility geocoding, urgency notes, and optional target donor pre-selection.
- Right: Preview screenshot `screenshots/06_emergency_request_form.png`.
- Target: Jumps to Slide 16 for Full-Screen HD View.

---

## Slide 16: 🔍 [Full-Screen HD View] Feature 5: Emergency Blood Request Form
- **High-Definition Screenshot**: Emergency request creation filled for Channagiri Taluk General Hospital, Main Road, Channagiri (2 Units of O_POSITIVE required).
- **Interactive Controls**: Click image to advance to Slide 17; click `↩ Overview` to return to Slide 15.

---

## Slide 17: Feature 6: Mutual Acceptance & Secure Handshake (Overview)

### Visual Layout
- Left: Two-way mutual consent rules (Patient dispatches request, Target Donor can Accept or Reject), contact info unlock upon acceptance, patient edit/close controls, and lifecycle closure.
- Right: Preview screenshot `screenshots/09_request_detail_matched.png`.
- Target: Jumps to Slide 18 for Full-Screen HD View.

---

## Slide 18: 🔍 [Full-Screen HD View] Feature 6: Mutual Acceptance Handshake
- **High-Definition Screenshot**: The "Connection established" panel unlocking the patient's verified contact phone number (`9876543210`) for Mariya Firdous in Channagiri, enabling urgent coordination, with complete lifecycle actions.
- **Interactive Controls**: Click image to advance to Slide 19; click `↩ Overview` to return to Slide 17.

---

## Slide 19: Feature 7: Administration Console & Governance (Overview)

### Visual Layout
- Left: Administrative oversight, user role escalation/revocation, emergency request moderation.
- Right: Preview screenshot `screenshots/10_admin_users.png`.
- Target: Jumps to Slide 20 for Full-Screen HD View.

---

## Slide 20: 🔍 [Full-Screen HD View] Feature 7: Administration Console
- **High-Definition Screenshot**: Administration user management console showing registered accounts, email addresses, blood groups, Channagiri localities, and privilege action links.
- **Interactive Controls**: Click image to advance to Slide 21; click `↩ Overview` to return to Slide 19.

---

## Slide 21: Database Design & Entity Relationships

### Tables Structure
- **`users` Table**: Primary entity storing credentials, blood group, encrypted password, city, area, and availability.
- **`user_roles` Table**: Join table enabling dynamic multi-role assignment (`DONOR`, `PATIENT`, `ADMIN`).
- **`blood_requests` Table**: Relational request model linking `patient_id` to `target_donor_id` (invited donor) and `accepted_donor_id` with statuses: `OPEN`, `ACCEPTED`, `COMPLETED`, `CANCELLED`, `REJECTED`, `CLOSED`.

---

## Slide 22: Project Architecture & Source Code Structure

### Source Package Organization
- **`com.smartblood`**: Bootstrap entrypoint (`SmartBloodDonorApplication.java`).
- **`com.smartblood.config`**: `SecurityConfig.java` (BCrypt, CSRF, URL authorizations).
- **`com.smartblood.controller`**: 7 Spring MVC controllers.
- **`com.smartblood.model`**: Entities (`AppUser`, `BloodRequest`) and Enums (`BloodGroup`, `Role`, `RequestStatus`).
- **`com.smartblood.repository`**: JPA repositories (`UserRepository`, `BloodRequestRepository`).
- **`com.smartblood.service`**: Business logic (`UserService`, `BloodRequestService`).
- **`src/main/resources`**: 14 Thymeleaf templates (including `request-edit.html`), `style.css`, `app.js`, `application.properties`.

---

## Slide 23: Deployment & Step-by-Step Installation Guide

### 4-Step Setup Flow
1. **Prerequisites**: JDK 11 (LTS), Apache Maven 3.6+, MySQL 8.0 on port 3306.
2. **Database Setup**: `CREATE DATABASE smart_blood_donor;` in MySQL.
3. **Build & Run**: Execute `mvn spring-boot:run` or double-click `build.bat`.
4. **Access Web App**: Navigate to `http://localhost:8080`.
5. **Admin Setup**: Register account, then run SQL promotion query to grant `ADMIN` privileges.

---

## Slide 24: Testing, Verification & Quality Assurance

### Key Bullet Points
- **Automated Test Suite**: 8 JUnit 5 test classes (51 automated tests, 100% pass) validating context startup, registration, role toggling, matching, request edit/close, donor accept/reject, and duplicate prevention.
- **Security Defenses**: BCrypt salted hashing, CSRF tokens on state-changing forms, strict RBAC filter chain, data redaction.

---

## Slide 25: Future Roadmap & Limitations

### Enhancement Pillars
- **Universal Compatibility Matrix**: Support clinical blood compatibility cross-matching (e.g. O- universal donors).
- **Twilio SMS & WhatsApp Dispatch**: Automated SMS broadcast alerts for emergency blood requests.
- **GPS Radius Distance Mapping**: Google Maps / OpenStreetMap API distance calculation.

---

## Slide 26: Conclusion & Academic Summary / Q&A

### Summary
- Bridges the emergency transfusion gap with privacy protection and locality filtering.
- Fully tested, secure, and ready for deployment.
- Complete documentation provided in Word (`.docx`), Markdown (`.md`), HTML (`.html`), and PowerPoint (`.pptx` & `.ppsx`).
