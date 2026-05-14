package com.hms.service;

import com.hms.dao.AppointmentDao;
import com.hms.dao.DoctorDao;
import com.hms.dao.PatientDao;
import com.hms.model.Appointment;
import com.hms.model.Doctor;
import com.hms.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class AppointmentService {

    @Autowired private AppointmentDao appointmentDao;
    @Autowired private PatientDao patientDao;
    @Autowired private DoctorDao doctorDao;

    public Appointment schedule(Appointment appointment) {
        if (appointment.getAppointmentDate() == null)
            throw new IllegalArgumentException("Appointment date is required");
        if (appointment.getAppointmentDate().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Appointment date cannot be in the past");
        return appointmentDao.save(appointment);
    }

    public Appointment getDetails(int id) {
        return appointmentDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + id));
    }

    public List<Appointment> getAllAppointments() {
        return appointmentDao.findAll();
    }

    public List<Appointment> getByPatient(int patientId) {
        return appointmentDao.findByPatient(patientId);
    }

    public List<Appointment> getByDoctor(int doctorId) {
        return appointmentDao.findByDoctor(doctorId);
    }

    public Appointment cancel(int id) {
        Appointment a = getDetails(id);
        a.setStatus(Appointment.AppointmentStatus.CANCELLED);
        return appointmentDao.update(a);
    }

    public Appointment update(Appointment appointment) {
        getDetails(appointment.getAppointmentId());
        return appointmentDao.update(appointment);
    }
}
