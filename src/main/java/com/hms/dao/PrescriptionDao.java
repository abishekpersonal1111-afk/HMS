package com.hms.dao;

import com.hms.model.Prescription;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PrescriptionDao implements GenericDao<Prescription, Integer> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Prescription save(Prescription p) {
        entityManager.persist(p);
        return p;
    }

    @Override
    public Optional<Prescription> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(Prescription.class, id));
    }

    @Override
    public List<Prescription> findAll() {
        return entityManager.createQuery("FROM Prescription ORDER BY dateIssued DESC", Prescription.class)
                .getResultList();
    }

    @Override
    public Prescription update(Prescription p) {
        return entityManager.merge(p);
    }

    @Override
    public void delete(Integer id) {
        Prescription p = entityManager.find(Prescription.class, id);
        if (p != null) entityManager.remove(p);
    }

    public List<Prescription> findByPatient(Integer patientId) {
        return entityManager.createQuery("FROM Prescription WHERE patient.patientId = :pid ORDER BY dateIssued DESC", Prescription.class)
                .setParameter("pid", patientId)
                .getResultList();
    }

    public List<Prescription> findByDoctor(Integer doctorId) {
        return entityManager.createQuery("FROM Prescription WHERE doctor.doctorId = :did ORDER BY dateIssued DESC", Prescription.class)
                .setParameter("did", doctorId)
                .getResultList();
    }
}

