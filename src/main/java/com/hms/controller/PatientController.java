package com.hms.controller;

import com.hms.model.Patient;
import com.hms.service.PatientService;
import com.hms.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) String search) {
        try {
            var result = (search != null && !search.isBlank())
                    ? patientService.search(search)
                    : patientService.getAllPatients();
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable int id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(patientService.getPatient(id)));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Patient patient) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Patient created", patientService.createPatient(patient)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody Patient patient) {
        try {
            patient.setPatientId(id);
            return ResponseEntity.ok(ApiResponse.ok("Patient updated", patientService.updatePatient(patient)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        try {
            patientService.deletePatient(id);
            return ResponseEntity.ok(ApiResponse.ok("Patient deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
