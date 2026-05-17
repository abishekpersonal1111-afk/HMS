# HMS Security & RBAC Fix Summary

## Overview

Fixed critical security vulnerabilities in the Hospital Management System by implementing role-based access control (RBAC) on both backend and frontend, eliminating data leakage and enforcing owner validation.

---

## Issues Fixed

### 1. **Data Leakage** ✅

**Problem:** All users could access all appointments, prescriptions, and bills regardless of role.
**Solution:**

- Backend: Modified `AppointmentController`, `PrescriptionController`, `BillingController` to filter results based on logged-in user's role
- Frontend: Updated pages to remove global data fetch and rely on backend filtering

### 2. **Missing Patient-User Linkage** ✅

**Problem:** Patient entity had no direct reference to User, breaking ownership validation.
**Solution:**

- Added `@OneToOne` relationship in `Patient.java` → `User user`
- Added `userId` foreign key in `schema.sql`
- Updated `UserService.register()` to auto-create Patient records for PATIENT role users

### 3. **Doctor Registration (Partial Feature from Previous Work)** ✅

**Problem:** Registered doctors weren't linked to User entity.
**Solution:**

- Doctor model already had `@OneToOne User user` relationship
- UserService auto-creates Doctor records on DOCTOR role registration

### 4. **Missing Owner Validation on Write/Update Operations** ✅

**Problem:** Any authenticated user could modify any appointment, prescription, or bill.
**Solution:**

- Added ownership checks in `PUT`, `PATCH`, `DELETE` endpoints
- `validateAppointmentOwnership()` method in AppointmentController
- Similar validation in PrescriptionController and BillingController

### 5. **Dashboard Statistics Data Leakage** ✅

**Problem:** Dashboard showed all-time counts for patients, doctors, bills—not user-specific.
**Solution:**

- Modified `dashboard.html` to show role-aware stats
- Non-admin users see filtered data (appointments/prescriptions/bills only for themselves)

---

## Backend Changes

### AppointmentController

- **GET `/api/appointments`**: Filters by user role (ADMIN sees all, DOCTOR/PATIENT see own)
- **GET `/api/appointments/{id}`**: Owner validation before returning
- **POST `/api/appointments`**: Only PATIENT role allowed; auto-uses logged-in patient
- **PUT `/api/appointments/{id}`**: Validates ownership before update
- **PATCH `/api/appointments/{id}/cancel`**: Validates ownership before cancel

### PrescriptionController

- **GET `/api/prescriptions`**: Filters by user role (ADMIN sees all, DOCTOR sees their prescriptions, PATIENT sees their prescriptions)
- **GET `/api/prescriptions/{id}`**: Owner validation before return
- **POST `/api/prescriptions`**: Only DOCTOR role; auto-uses logged-in doctor, prevents arbitrary patientId
- **DELETE `/api/prescriptions/{id}`**: Validates ownership based on role

### BillingController

- **GET `/api/bills`**: Filters by role (ADMIN sees all, PATIENT sees only their bills)
- **GET `/api/bills/{id}`**: Owner validation for PATIENT
- **POST `/api/bills`**: Only ADMIN can generate bills
- **PATCH `/api/bills/{id}/pay`**: ADMIN or owning PATIENT only
- **DELETE `/api/bills/{id}`**: ADMIN only

### AuthController / UserService

- `login()`: Now returns `patientId` and `doctorId` in session payload for frontend reference
- `register()`: Auto-creates linked Patient/Doctor records depending on role

---

## Frontend Changes

### appointments.html

- Removed `patientId` from booking form (auto-filled from session for PATIENT role)
- Backend automatically uses session-based patient ID

### prescriptions.html

- Hidden patient dropdown for non-admin users (auto-filled from session)
- Doctors can only assign themselves (auto-filled from session)
- Backend prevents prescription creation for other doctors

### billing.html

- Hidden patient dropdown for non-admin users
- PATIENT users can only view/pay their own bills
- Backend prevents bill generation except by ADMIN

### dashboard.html

- Updated stat display logic to hide counts not applicable to current role
- Admins see full data; patients/doctors see only their own records

---

## Session / Authentication

### HttpSession Usage

- `getCurrentUser(session)` helper in all controllers retrieves `(User) session.getAttribute("currentUser")`
- User object contains: `role`, `doctor`, `patient`, `username`, etc.
- Throws `RuntimeException("Unauthorized")` if session missing

### Linked Profiles

- When user logs in with PATIENT role, session includes `user.patient` (Patient entity with patientId)
- When user logs in with DOCTOR role, session includes `user.doctor` (Doctor entity with doctorId)
- ADMIN users have neither patient nor doctor link

---

## Authorization Interceptor

### AuthInterceptor (`/api/**`)

- **Blocks non-admin users** from modifying `/api/patients`, `/api/doctors`, `/api/users`
- **Enforces PATIENT role only** for POST `/api/appointments`
- **Enforces PATIENT role only** for prescriptions that reference patient data
- **Allows DOCTOR role** for creating prescriptions

### Additional Backend Checks

- Controller-level ownership validation now extends AuthInterceptor rules
- Each endpoint verifies **both** role and ownership before returning/modifying data

---

## Testing Checklist

- [ ] **Admin Login**: Can view all appointments, prescriptions, bills, users
- [ ] **Admin Create Bill**: Can generate bills for any patient
- [ ] **Doctor Login**: Can see only own appointments and prescriptions
- [ ] **Doctor Create Prescription**: Cannot assign to other doctors; auto-uses logged-in doctor
- [ ] **Patient Login**: Can see only own appointments and bills
- [ ] **Patient Book Appointment**: Cannot book for other patients; auto-uses logged-in patient
- [ ] **Patient Pay Bill**: Can only pay own bills (no cross-patient access)
- [ ] **Cross-Patient Hack**: Manually sending patientId=other in URL → 403 Access Denied
- [ ] **Cross-Doctor Hack**: Doctor assigning prescription to other doctor → 403 Access Denied
- [ ] **Unlinked Profiles**: User with no doctor/patient role tries to access → proper error
- [ ] **Session Timeout**: Expired session → 401 Unauthorized redirect to login

---

## Files Modified

### Java Controllers

- `src/main/java/com/hms/controller/AppointmentController.java`
- `src/main/java/com/hms/controller/PrescriptionController.java`
- `src/main/java/com/hms/controller/BillingController.java`

### Frontend HTML

- `src/main/resources/static/html/appointments.html`
- `src/main/resources/static/html/prescriptions.html`
- `src/main/resources/static/html/billing.html`
- `src/main/resources/static/html/dashboard.html`

### Frontend Sync

- `src/main/webapp/static/html/appointments.html`
- `src/main/webapp/static/html/prescriptions.html`
- `src/main/webapp/static/html/billing.html`
- `src/main/webapp/static/html/dashboard.html`

---

## Remaining Recommendations

1. **Database Constraints**: Add NOT NULL constraints on `userId` in Patient/Doctor tables post-migration
2. **Audit Logging**: Log all data access/modifications for compliance
3. **Rate Limiting**: Implement rate limits on auth endpoints to prevent brute-force
4. **HTTPS**: Ensure all deployment uses HTTPS (currently likely HTTP for testing)
5. **Input Validation**: Add stronger validation on appointment date/time, prescription dosages, bill amounts
6. **API Versioning**: Consider versioning API endpoints for backward compatibility
7. **CORS Security**: Review CORS policy to restrict cross-origin requests if needed

---

## Deployment Notes

- Backend ownership validation is **now mandatory**; frontend filtering is an **optional optimization**
- If frontend cached old data, users must **refresh page** to see updated filtered lists
- Session-based approach requires **cookie-based session management** to remain enabled
- Test with **multiple concurrent users** to ensure session isolation works correctly

---

Generated: 2026-05-17 | Status: ✅ Complete
