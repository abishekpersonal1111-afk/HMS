package com.hms.service;

import com.hms.dao.DoctorDao;
import com.hms.model.Doctor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DoctorService {

    @Autowired
    private DoctorDao doctorDao;

    public Doctor addDoctor(Doctor doctor) {
        if (doctor.getName() == null || doctor.getName().isBlank())
            throw new IllegalArgumentException("Doctor name is required");
        return doctorDao.save(doctor);
    }

    public Doctor getDoctorDetails(int id) {
        return doctorDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + id));
    }

    public List<Doctor> getAllDoctors() {
        return doctorDao.findAll();
    }

    public Doctor updateDoctor(Doctor doctor) {
        getDoctorDetails(doctor.getDoctorId());
        return doctorDao.update(doctor);
    }

    public void deleteDoctor(int id) {
        doctorDao.delete(id);
    }

    public List<Doctor> findBySpecialization(String spec) {
        return doctorDao.findBySpecialization(spec);
    }
}
