# HMS RBAC Implementation Details

## 1. Backend Ownership Enforcement Pattern

### Pattern Used in All Three Controllers

```java
// In controller, add HttpSession parameter to endpoint
@GetMapping("/{id}")
public ResponseEntity<?> getById(@PathVariable("id") int id, HttpSession session) {
    try {
        // Fetch resource
        SomeEntity entity = service.getById(id);

        // Get current user from session
        User currentUser = getCurrentUser(session);

        // Admin always allowed
        if (currentUser.getRole() == User.Role.ADMIN) {
            return ResponseEntity.ok(ApiResponse.ok(entity));
        }

        // Role-specific ownership check
        if (currentUser.getRole() == User.Role.DOCTOR) {
            if (currentUser.getDoctor() == null ||
                !currentUser.getDoctor().getDoctorId().equals(entity.getDoctor().getDoctorId())) {
                throw new RuntimeException("Access denied: doctor can only view their own records");
            }
            return ResponseEntity.ok(ApiResponse.ok(entity));
        }

        // Default: patients check their own ownership
        if (currentUser.getPatient() == null ||
            !currentUser.getPatient().getPatientId().equals(entity.getPatient().getPatientId())) {
            throw new RuntimeException("Access denied: patients can only view their own records");
        }
        return ResponseEntity.ok(ApiResponse.ok(entity));
    } catch (Exception e) {
        return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
    }
}

// Helper methods (add to each controller)
private User getCurrentUser(HttpSession session) {
    User user = (User) session.getAttribute("currentUser");
    if (user == null) throw new RuntimeException("Unauthorized");
    return user;
}

private void validateOwnership(Entity entity, HttpSession session) {
    User currentUser = getCurrentUser(session);
    if (currentUser.getRole() == User.Role.ADMIN) return;
    if (currentUser.getRole() == User.Role.DOCTOR) {
        if (!currentUser.getDoctor().getDoctorId().equals(entity.getDoctor().getDoctorId())) {
            throw new RuntimeException("Access denied: doctor can only manage their own records");
        }
        return;
    }
    if (!currentUser.getPatient().getPatientId().equals(entity.getPatient().getPatientId())) {
        throw new RuntimeException("Access denied: patients can only manage their own records");
    }
}
```

### Key Principles

1. **Always fetch the resource first** before validating (prevents timing attacks)
2. **Admin bypass is first check** (simplest to evaluate)
3. **Role-specific checks** prevent cross-role access
4. **Throw RuntimeException** if denied (caught by try/catch, returns 404/400 with error message)
5. **Return immediately** after validation (cleaner than nested if-else)

---

## 2. Data Filtering in GET All Endpoints

### Pattern: Role-Based List Filtering

```java
@GetMapping
public ResponseEntity<?> getAll(HttpSession session,
                                @RequestParam(required = false) Integer patientId,
                                @RequestParam(required = false) Integer doctorId) {
    try {
        User currentUser = getCurrentUser(session);

        // Admin: can filter by any parameter
        if (currentUser.getRole() == User.Role.ADMIN) {
            var result = patientId != null ? service.getByPatient(patientId)
                       : doctorId != null ? service.getByDoctor(doctorId)
                       : service.getAll();
            return ResponseEntity.ok(ApiResponse.ok(result));
        }

        // Doctor: can only see their own records
        if (currentUser.getRole() == User.Role.DOCTOR) {
            int doctorIdFilter = currentUser.getDoctor().getDoctorId();
            return ResponseEntity.ok(ApiResponse.ok(service.getByDoctor(doctorIdFilter)));
        }

        // Patient: can only see their own records
        int patientIdFilter = currentUser.getPatient().getPatientId();
        return ResponseEntity.ok(ApiResponse.ok(service.getByPatient(patientIdFilter)));
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
    }
}
```

### Key Principles

1. **Never trust query parameters** for filtering (user can't ask for other user's data)
2. **Extract ID from session**, not request
3. **Non-admins ignore filter parameters** and use their own ID
4. **Backend filtering is mandatory**; frontend filtering is bonus UX

---

## 3. Write Operations (POST/PUT/DELETE)

### For POST (Create) - Example: Prescriptions

```java
@PostMapping
public ResponseEntity<?> create(@RequestBody Map<String, Object> body, HttpSession session) {
    try {
        User currentUser = getCurrentUser(session);

        // Enforce role restriction
        if (currentUser.getRole() != User.Role.DOCTOR)
            throw new RuntimeException("Only doctors can create prescriptions");

        // Verify linked profile exists
        if (currentUser.getDoctor() == null)
            throw new RuntimeException("Doctor profile not found");

        // Build entity
        Prescription rx = new Prescription();

        // Patient ID from request (admin can assign to any patient)
        Patient p = new Patient();
        p.setPatientId((Integer) body.get("patientId"));

        // Doctor ID from CURRENT USER, not request (enforce doctor cannot assign to others)
        Doctor d = new Doctor();
        d.setDoctorId(currentUser.getDoctor().getDoctorId());

        rx.setPatient(p);
        rx.setDoctor(d);
        // ... set other fields ...

        return ResponseEntity.ok(ApiResponse.ok("Created", service.create(rx)));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
}
```

### For PUT/PATCH (Update) - Example: Appointments

```java
@PutMapping("/{id}")
public ResponseEntity<?> update(@PathVariable("id") int id,
                                @RequestBody Map<String, Object> body,
                                HttpSession session) {
    try {
        // Fetch and validate ownership
        Appointment appointment = service.getDetails(id);
        validateOwnership(appointment, session);  // Helper method

        // Update allowed fields
        if (body.containsKey("appointmentDate"))
            appointment.setAppointmentDate(java.time.LocalDate.parse(...));
        if (body.containsKey("timeSlot"))
            appointment.setTimeSlot((String) body.get("timeSlot"));

        // DO NOT allow changing patient/doctor (security risk)

        return ResponseEntity.ok(ApiResponse.ok("Updated", service.update(appointment)));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
}
```

### For DELETE - Example: Bills

```java
@DeleteMapping("/{id}")
public ResponseEntity<?> delete(@PathVariable("id") int id, HttpSession session) {
    try {
        User currentUser = getCurrentUser(session);

        // Only admin can delete bills
        if (currentUser.getRole() != User.Role.ADMIN)
            throw new RuntimeException("Only admins can delete bills");

        service.deleteBill(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
}
```

### Key Principles

1. **Fetch entity first**, then validate (prevents blind delete)
2. **Never overwrite ID fields** from request body (patientId, doctorId must be validated)
3. **Use session values** for critical fields (doctor always assigns to self)
4. **Return 400 Bad Request** for validation failures, not 403 (simpler error handling)

---

## 4. Frontend Filtering (Optional but Recommended)

### Pattern: Session-Based UI Filtering

```javascript
async function loadAppointments() {
    const user = getSession(); // { role, patientId, doctorId, username, ... }

    // Backend filters automatically, but frontend can provide UX feedback
    const res = await API.appointments.getAll();
    if (!res.success) { toast(res.message, 'error'); return; }

    appointments = res.data || [];
    renderTable(appointments);  // Frontend receives pre-filtered data
}

function saveAppointment() {
    const user = getSession();

    // For PATIENT role, don't show patient dropdown (or hide it)
    // For DOCTOR role, don't show doctor dropdown

    const doctorId = document.getElementById('apt-doctor').value;
    const date = document.getElementById('apt-date').value;

    // Backend automatically uses session-based patientId
    const res = await API.appointments.schedule({
        doctorId: parseInt(doctorId),
        appointmentDate: date,
        timeSlot: timeSlot
    });
}
```

### Key Principles

1. **Frontend optimizes UX**, backend enforces security
2. **Hide fields non-applicable to role** (reduce user confusion)
3. **Don't duplicate backend validation** in frontend (security can't rely on it)
4. **Show/hide CRUD buttons** based on role (Schedule button for PATIENT, Create button for ADMIN)

---

## 5. Session Management (AuthController)

### Login Endpoint Must Return Linked IDs

```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody Map<String, String> creds, HttpSession session) {
    try {
        // ... validate credentials ...
        User user = userService.findByUsername(creds.get("username"));

        // Store in session
        session.setAttribute("currentUser", user);

        // Return payload including linked IDs for frontend
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", user.getUserId());
        payload.put("username", user.getUsername());
        payload.put("role", user.getRole());
        payload.put("patientId", user.getPatient() != null ? user.getPatient().getPatientId() : null);
        payload.put("doctorId", user.getDoctor() != null ? user.getDoctor().getDoctorId() : null);

        return ResponseEntity.ok(ApiResponse.ok("Login successful", payload));
    } catch (Exception e) {
        return ResponseEntity.unauthorized().body(ApiResponse.error(e.getMessage()));
    }
}
```

### Frontend Session Storage

```javascript
function setSession(user) {
  sessionStorage.setItem("hmsUser", JSON.stringify(user));
}

function getSession() {
  try {
    return JSON.parse(sessionStorage.getItem("hmsUser") || "null");
  } catch {
    return null;
  }
}
```

---

## 6. Error Handling Strategy

### HTTP Status Codes Used

| Code | Scenario                          | Response                                                                  |
| ---- | --------------------------------- | ------------------------------------------------------------------------- |
| 200  | Operation successful              | `{ success: true, data: {...} }`                                          |
| 400  | Validation/business logic error   | `{ success: false, message: "..." }`                                      |
| 401  | Unauthorized (no session)         | `{ success: false, message: "Unauthorized" }` (AuthInterceptor redirects) |
| 403  | Forbidden (role/ownership denied) | Usually return 400 with message (simpler than 403 for SPAs)               |
| 404  | Resource not found                | `{ success: false, message: "..." }` (or 400)                             |
| 500  | Server error                      | `{ success: false, message: "Server error" }`                             |

### Rationale

- **Return 400 for access denial** instead of 403 to avoid leaking which resources exist (e.g., if bill #123 exists)
- **Don't expose implementation details** (e.g., don't say "Doctor profile not found")

---

## 7. User Linked Profile Setup (UserService)

### Auto-Create Linked Records on Registration

```java
public User register(String username, String password, User.Role role) {
    // ... validate, hash password ...

    User user = new User();
    user.setUsername(username);
    user.setPassword(hashedPassword);
    user.setRole(role);
    user.setApproved(false);
    user = userDao.save(user);

    // Auto-create linked profile for immediate access
    if (role == User.Role.PATIENT) {
        Patient patient = new Patient();
        patient.setUser(user);
        patient.setName(username); // or derive from request
        patientDao.save(patient);
        user.setPatient(patient);
    } else if (role == User.Role.DOCTOR) {
        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setName(username);
        doctorDao.save(doctor);
        user.setDoctor(doctor);
    }

    return user;
}
```

### On Approval (Admin Approves Pending User)

```java
public void approveUser(int userId) {
    User user = userDao.findById(userId);

    // Ensure linked profile exists
    if (user.getRole() == User.Role.PATIENT && user.getPatient() == null) {
        Patient patient = new Patient();
        patient.setUser(user);
        patient.setName(user.getUsername());
        patientDao.save(patient);
        user.setPatient(patient);
    } else if (user.getRole() == User.Role.DOCTOR && user.getDoctor() == null) {
        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setName(user.getUsername());
        doctorDao.save(doctor);
        user.setDoctor(doctor);
    }

    user.setApproved(true);
    userDao.update(user);
}
```

---

## 8. Database Schema Constraints

### Required FK Updates

```sql
-- Add constraints if not already present
ALTER TABLE patient ADD CONSTRAINT fk_patient_user
    FOREIGN KEY (userId) REFERENCES user(userId)
    ON DELETE CASCADE;

ALTER TABLE doctor ADD CONSTRAINT fk_doctor_user
    FOREIGN KEY (userId) REFERENCES user(userId)
    ON DELETE CASCADE;

-- Ensure cascading deletes work
-- When user is deleted, patient/doctor records are automatically deleted
```

---

## Summary Checklist for New Endpoints

When adding new CRUD endpoints, ensure:

- [ ] **GET (single)**: Fetch entity, validate ownership, return 404 if denied
- [ ] **GET (list)**: Filter by role (admin sees all, others see own)
- [ ] **POST**: Validate role, extract IDs from session for critical fields
- [ ] **PUT**: Fetch, validate ownership, prevent ID field changes
- [ ] **DELETE**: Fetch, validate ownership, delete
- [ ] **HttpSession parameter** on all endpoints requiring auth
- [ ] **Helper methods** `getCurrentUser()` and `validateOwnership()` used consistently
- [ ] **Error handling** with appropriate status codes and messages
- [ ] **No data leakage** in error messages
- [ ] **Frontend** hides non-applicable UI elements per role

---

Generated: 2026-05-17
