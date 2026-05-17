package com.hms.controller;

import com.hms.model.Doctor;
import com.hms.model.Medicine;
import com.hms.model.Patient;
import com.hms.model.Prescription;
import com.hms.model.User;
import com.hms.service.PrescriptionService;
import com.hms.util.ApiResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    @GetMapping
    public ResponseEntity<?> getAll(HttpSession session,
            @RequestParam(required = false) Integer patientId,
            @RequestParam(required = false) Integer doctorId) {
        try {
            User currentUser = getCurrentUser(session);
            if (currentUser.getRole() == User.Role.ADMIN) {
                var result = patientId != null ? prescriptionService.getByPatient(patientId)
                        : doctorId != null ? prescriptionService.getByDoctor(doctorId)
                                : prescriptionService.getAll();
                return ResponseEntity.ok(ApiResponse.ok(result));
            }
            if (currentUser.getRole() == User.Role.DOCTOR) {
                int doctorIdFilter = currentUser.getDoctor().getDoctorId();
                return ResponseEntity.ok(ApiResponse.ok(prescriptionService.getByDoctor(doctorIdFilter)));
            }
            int patientIdFilter = currentUser.getPatient().getPatientId();
            return ResponseEntity.ok(ApiResponse.ok(prescriptionService.getByPatient(patientIdFilter)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") int id, HttpSession session) {
        try {
            Prescription prescription = prescriptionService.getById(id);
            User currentUser = getCurrentUser(session);
            if (currentUser.getRole() == User.Role.ADMIN) {
                return ResponseEntity.ok(ApiResponse.ok(prescription));
            }
            if (currentUser.getRole() == User.Role.DOCTOR) {
                if (currentUser.getDoctor() == null
                        || !currentUser.getDoctor().getDoctorId().equals(prescription.getDoctor().getDoctorId()))
                    throw new RuntimeException("Access denied: doctor can only view their own prescriptions");
                return ResponseEntity.ok(ApiResponse.ok(prescription));
            }
            if (currentUser.getPatient() == null
                    || !currentUser.getPatient().getPatientId().equals(prescription.getPatient().getPatientId()))
                throw new RuntimeException("Access denied: patients can only view their own prescriptions");
            return ResponseEntity.ok(ApiResponse.ok(prescription));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body, HttpSession session) {
        try {
            User currentUser = getCurrentUser(session);
            if (currentUser.getRole() != User.Role.DOCTOR)
                throw new RuntimeException("Only doctors can create prescriptions");
            if (currentUser.getDoctor() == null)
                throw new RuntimeException("Doctor profile not found");

            Prescription rx = new Prescription();
            Patient p = new Patient();
            p.setPatientId((Integer) body.get("patientId"));
            Doctor d = new Doctor();
            d.setDoctorId(currentUser.getDoctor().getDoctorId());
            rx.setPatient(p);
            rx.setDoctor(d);
            rx.setDiagnosis((String) body.get("diagnosis"));
            rx.setDateIssued(LocalDate.now());

            List<Map<String, String>> meds = (List<Map<String, String>>) body.get("medicines");
            if (meds != null) {
                for (Map<String, String> m : meds) {
                    Medicine med = new Medicine();
                    med.setPrescription(rx);
                    med.setMedicineName(m.get("medicineName"));
                    med.setDosage(m.get("dosage"));
                    med.setFrequency(m.get("frequency"));
                    med.setDuration(m.get("duration"));
                    rx.getMedicines().add(med);
                }
            }
            return ResponseEntity.ok(ApiResponse.ok("Prescription created", prescriptionService.create(rx)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") int id, HttpSession session) {
        try {
            Prescription prescription = prescriptionService.getById(id);
            User currentUser = getCurrentUser(session);
            if (currentUser.getRole() != User.Role.ADMIN) {
                if (currentUser.getRole() == User.Role.DOCTOR) {
                    if (currentUser.getDoctor() == null
                            || !currentUser.getDoctor().getDoctorId().equals(prescription.getDoctor().getDoctorId()))
                        throw new RuntimeException("Access denied: doctor can only delete their own prescriptions");
                } else {
                    if (currentUser.getPatient() == null || !currentUser.getPatient().getPatientId()
                            .equals(prescription.getPatient().getPatientId()))
                        throw new RuntimeException("Access denied: patients can only delete their own prescriptions");
                }
            }
            prescriptionService.delete(id);
            return ResponseEntity.ok(ApiResponse.ok("Prescription deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    private User getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null)
            throw new RuntimeException("Unauthorized");
        return user;
    }}try{

    Prescription rx = new Prescription();
    Patient p = new Patient();p.setPatientId((Integer)body.get("patientId"));
    Doctor d = new Doctor();d.setDoctorId((Integer)body.get("doctorId"));rx.setPatient(p);rx.setDoctor(d);rx.setDiagnosis((String)body.get("diagnosis"));rx.setDateIssued(LocalDate.now());

    List<Map<String, String>> meds = (List<Map<String, String>>) body.get("medicines");if(meds!=null)
    {
        for (Map<String, String> m : meds) {
            Medicine med = new Medicine();
            med.setPrescription(rx);
            med.setMedicineName(m.get("medicineName"));
            med.setDosage(m.get("dosage"));
            med.setFrequency(m.get("frequency"));
            med.setDuration(m.get("duration"));
            rx.getMedicines().add(med);
        }
    }return ResponseEntity.ok(ApiResponse.ok("Prescription created",prescriptionService.create(rx)));}catch(
    Exception e)
    {
        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") int id) {
        try {
            prescriptionService.delete(id);
            return ResponseEntity.ok(ApiResponse.ok("Prescription deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
