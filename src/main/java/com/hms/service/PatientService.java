package com.hms.service;

import com.hms.dao.PatientDao;
import com.hms.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PatientService {

    @Autowired
    private PatientDao patientDao;

    public Patient createPatient(Patient patient) {
        if (patient.getName() == null || patient.getName().isBlank())
            throw new IllegalArgumentException("Patient name is required");
        return patientDao.save(patient);
    }

    public Patient getPatient(int id) {
        return patientDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + id));
    }

    public List<Patient> getAllPatients() {
        return patientDao.findAll();
    }

    public Patient updatePatient(Patient patient) {
        getPatient(patient.getPatientId()); // validates existence
        return patientDao.update(patient);
    }

    public void deletePatient(int id) {
        patientDao.delete(id);
    }

    public List<Patient> search(String keyword) {
        return patientDao.search(keyword);
    }
}
