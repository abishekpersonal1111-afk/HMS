package com.hms.controller;

import com.hms.model.Appointment;
import com.hms.model.Doctor;
import com.hms.model.Patient;
import com.hms.service.AppointmentService;
import com.hms.util.ApiResponse;
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
            @RequestParam(required = false) Integer doctorId) {
        try {
            var result = patientId != null ? appointmentService.getByPatient(patientId)
                       : doctorId  != null ? appointmentService.getByDoctor(doctorId)
                       : appointmentService.getAllAppointments();
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable int id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(appointmentService.getDetails(id)));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> schedule(@RequestBody Map<String, Object> body) {
        try {
            Appointment a = new Appointment();
            Patient p = new Patient(); p.setPatientId((Integer) body.get("patientId"));
            Doctor  d = new Doctor();  d.setDoctorId((Integer) body.get("doctorId"));
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
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody Map<String, Object> body) {
        try {
            Appointment a = appointmentService.getDetails(id);
            if (body.containsKey("appointmentDate"))
                a.setAppointmentDate(java.time.LocalDate.parse((String) body.get("appointmentDate")));
            if (body.containsKey("timeSlot"))     a.setTimeSlot((String) body.get("timeSlot"));
            if (body.containsKey("status"))
                a.setStatus(Appointment.AppointmentStatus.valueOf((String) body.get("status")));
            return ResponseEntity.ok(ApiResponse.ok("Appointment updated", appointmentService.update(a)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable int id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Appointment cancelled", appointmentService.cancel(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
