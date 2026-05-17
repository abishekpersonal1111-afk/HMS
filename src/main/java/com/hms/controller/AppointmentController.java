package com.hms.controller;

import com.hms.model.Appointment;
import com.hms.model.Doctor;
import com.hms.model.Patient;
import com.hms.model.User;
import com.hms.service.AppointmentService;
import com.hms.util.ApiResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) Integer patientId,
            @RequestParam(required = false) Integer doctorId,
            HttpSession session) {
        try {
            User currentUser = getCurrentUser(session);
            if (currentUser.getRole() == User.Role.ADMIN) {
                var result = patientId != null ? appointmentService.getByPatient(patientId)
                        : doctorId != null ? appointmentService.getByDoctor(doctorId)
                                : appointmentService.getAllAppointments();
                return ResponseEntity.ok(ApiResponse.ok(result));
            }
            if (currentUser.getRole() == User.Role.DOCTOR) {
                if (currentUser.getDoctor() == null)
                    throw new RuntimeException("Doctor profile not found");
                int currentDoctorId = currentUser.getDoctor().getDoctorId();
                if (doctorId != null && !doctorId.equals(currentDoctorId))
                    throw new RuntimeException("Access denied: doctor can only view their own appointments");
                return ResponseEntity.ok(ApiResponse.ok(appointmentService.getByDoctor(currentDoctorId)));
            }

            if (currentUser.getPatient() == null)
                throw new RuntimeException("Patient profile not found");
            int currentPatientId = currentUser.getPatient().getPatientId();
            if (patientId != null && !patientId.equals(currentPatientId))
                throw new RuntimeException("Access denied: patients can only view their own appointments");
            return ResponseEntity.ok(ApiResponse.ok(appointmentService.getByPatient(currentPatientId)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") int id, HttpSession session) {
        try {
            Appointment appointment = appointmentService.getDetails(id);
            User currentUser = getCurrentUser(session);
            if (currentUser.getRole() == User.Role.ADMIN) {
                return ResponseEntity.ok(ApiResponse.ok(appointment));
            }
            if (currentUser.getRole() == User.Role.DOCTOR) {
                if (currentUser.getDoctor() == null
                        || !currentUser.getDoctor().getDoctorId().equals(appointment.getDoctor().getDoctorId()))
                    throw new RuntimeException("Access denied: doctor can only view their own appointments");
                return ResponseEntity.ok(ApiResponse.ok(appointment));
            }
            if (currentUser.getPatient() == null
                    || !currentUser.getPatient().getPatientId().equals(appointment.getPatient().getPatientId()))
                throw new RuntimeException("Access denied: patients can only view their own appointments");
            return ResponseEntity.ok(ApiResponse.ok(appointment));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> schedule(@RequestBody Map<String, Object> body, HttpSession session) {
        try {
            User currentUser = getCurrentUser(session);
            if (currentUser.getRole() != User.Role.PATIENT)
                throw new RuntimeException("Only patients can book appointments");
            if (currentUser.getPatient() == null)
                throw new RuntimeException("Patient profile not found");

            Appointment a = new Appointment();
            Patient p = new Patient();
            p.setPatientId(currentUser.getPatient().getPatientId());
            Doctor d = new Doctor();
            d.setDoctorId((Integer) body.get("doctorId"));
            a.setPatient(p);
            a.setDoctor(d);
            a.setAppointmentDate(java.time.LocalDate.parse((String) body.get("appointmentDate")));
            a.setTimeSlot((String) body.get("timeSlot"));
            a.setStatus(Appointment.AppointmentStatus.CONFIRMED);
            return ResponseEntity.ok(ApiResponse.ok("Appointment scheduled", appointmentService.schedule(a)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") int id, @RequestBody Map<String, Object> body,
            HttpSession session) {
        try {
            Appointment appointment = appointmentService.getDetails(id);
            validateAppointmentOwnership(appointment, session);
            if (body.containsKey("appointmentDate"))
                appointment.setAppointmentDate(java.time.LocalDate.parse((String) body.get("appointmentDate")));
            if (body.containsKey("timeSlot"))
                appointment.setTimeSlot((String) body.get("timeSlot"));
            if (body.containsKey("status"))
                appointment.setStatus(Appointment.AppointmentStatus.valueOf((String) body.get("status")));
            return ResponseEntity.ok(ApiResponse.ok("Appointment updated", appointmentService.update(appointment)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable("id") int id, HttpSession session) {
        try {
            Appointment appointment = appointmentService.getDetails(id);
            validateAppointmentOwnership(appointment, session);
            return ResponseEntity.ok(ApiResponse.ok("Appointment cancelled", appointmentService.cancel(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    private User getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null)
            throw new RuntimeException("Unauthorized");
        return user;
    }

    private void validateAppointmentOwnership(Appointment appointment, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser.getRole() == User.Role.ADMIN)
            return;
        if (currentUser.getRole() == User.Role.DOCTOR) {
            if (currentUser.getDoctor() == null
                    || !currentUser.getDoctor().getDoctorId().equals(appointment.getDoctor().getDoctorId()))
                throw new RuntimeException("Access denied: doctor can only manage their own appointments");
            return;
        }
        if (currentUser.getPatient() == null
                || !currentUser.getPatient().getPatientId().equals(appointment.getPatient().getPatientId()))
            throw new RuntimeException("Access denied: patients can only manage their own appointments");
    }
}
