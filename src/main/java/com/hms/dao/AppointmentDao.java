package com.hms.dao;

import com.hms.model.Appointment;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class AppointmentDao implements GenericDao<Appointment, Integer> {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public Appointment save(Appointment a) {
        sessionFactory.getCurrentSession().persist(a);
        return a;
    }

    @Override
    public Optional<Appointment> findById(Integer id) {
        return Optional.ofNullable(sessionFactory.getCurrentSession().get(Appointment.class, id));
    }

    @Override
    public List<Appointment> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Appointment ORDER BY appointmentDate DESC", Appointment.class)
                .getResultList();
    }

    @Override
    public Appointment update(Appointment a) {
        return (Appointment) sessionFactory.getCurrentSession().merge(a);
    }

    @Override
    public void delete(Integer id) {
        Appointment a = sessionFactory.getCurrentSession().get(Appointment.class, id);
        if (a != null) sessionFactory.getCurrentSession().remove(a);
    }

    public List<Appointment> findByPatient(Integer patientId) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Appointment WHERE patient.patientId = :pid ORDER BY appointmentDate DESC", Appointment.class)
                .setParameter("pid", patientId)
                .getResultList();
    }

    public List<Appointment> findByDoctor(Integer doctorId) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Appointment WHERE doctor.doctorId = :did ORDER BY appointmentDate DESC", Appointment.class)
                .setParameter("did", doctorId)
                .getResultList();
    }

    public List<Appointment> findByDate(LocalDate date) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Appointment WHERE appointmentDate = :d", Appointment.class)
                .setParameter("d", date)
                .getResultList();
    }
}
