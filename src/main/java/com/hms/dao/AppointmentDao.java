package com.hms.dao;

import com.hms.model.Appointment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class AppointmentDao implements GenericDao<Appointment, Integer> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Appointment save(Appointment a) {
        entityManager.persist(a);
        return a;
    }

    @Override
    public Optional<Appointment> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(Appointment.class, id));
    }

    @Override
    public List<Appointment> findAll() {
        return entityManager.createQuery("FROM Appointment ORDER BY appointmentDate DESC", Appointment.class)
                .getResultList();
    }

    @Override
    public Appointment update(Appointment a) {
        return entityManager.merge(a);
    }

    @Override
    public void delete(Integer id) {
        Appointment a = entityManager.find(Appointment.class, id);
        if (a != null) entityManager.remove(a);
    }

    public List<Appointment> findByPatient(Integer patientId) {
        return entityManager.createQuery("FROM Appointment WHERE patient.patientId = :pid ORDER BY appointmentDate DESC", Appointment.class)
                .setParameter("pid", patientId)
                .getResultList();
    }

    public List<Appointment> findByDoctor(Integer doctorId) {
        return entityManager.createQuery("FROM Appointment WHERE doctor.doctorId = :did ORDER BY appointmentDate DESC", Appointment.class)
                .setParameter("did", doctorId)
                .getResultList();
    }

    public List<Appointment> findByDate(LocalDate date) {
        return entityManager.createQuery("FROM Appointment WHERE appointmentDate = :d", Appointment.class)
                .setParameter("d", date)
                .getResultList();
    }
}
