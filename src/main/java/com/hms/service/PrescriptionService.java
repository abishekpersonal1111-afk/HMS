package com.hms.service;

import com.hms.dao.PrescriptionDao;
import com.hms.model.Prescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class PrescriptionService {

    @Autowired
    private PrescriptionDao prescriptionDao;

    public Prescription create(Prescription prescription) {
        if (prescription.getDateIssued() == null) prescription.setDateIssued(LocalDate.now());
        return prescriptionDao.save(prescription);
    }

    public Prescription getById(int id) {
        return prescriptionDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + id));
    }

    public List<Prescription> getAll() {
        return prescriptionDao.findAll();
    }

    public List<Prescription> getByPatient(int patientId) {
        return prescriptionDao.findByPatient(patientId);
    }

    public List<Prescription> getByDoctor(int doctorId) {
        return prescriptionDao.findByDoctor(doctorId);
    }

    public Prescription update(Prescription prescription) {
        getById(prescription.getPrescriptionId());
        return prescriptionDao.update(prescription);
    }

    public void delete(int id) {
        prescriptionDao.delete(id);
    }
}
