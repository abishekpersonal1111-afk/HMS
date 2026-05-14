package com.hms.dao;

import com.hms.model.Prescription;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class PrescriptionDao implements GenericDao<Prescription, Integer> {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public Prescription save(Prescription p) {
        sessionFactory.getCurrentSession().persist(p);
        return p;
    }

    @Override
    public Optional<Prescription> findById(Integer id) {
        return Optional.ofNullable(sessionFactory.getCurrentSession().get(Prescription.class, id));
    }

    @Override
    public List<Prescription> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Prescription ORDER BY dateIssued DESC", Prescription.class)
                .getResultList();
    }

    @Override
    public Prescription update(Prescription p) {
        return (Prescription) sessionFactory.getCurrentSession().merge(p);
    }

    @Override
    public void delete(Integer id) {
        Prescription p = sessionFactory.getCurrentSession().get(Prescription.class, id);
        if (p != null) sessionFactory.getCurrentSession().remove(p);
    }

    public List<Prescription> findByPatient(Integer patientId) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Prescription WHERE patient.patientId = :pid ORDER BY dateIssued DESC", Prescription.class)
                .setParameter("pid", patientId)
                .getResultList();
    }

    public List<Prescription> findByDoctor(Integer doctorId) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Prescription WHERE doctor.doctorId = :did ORDER BY dateIssued DESC", Prescription.class)
                .setParameter("did", doctorId)
                .getResultList();
    }
}
