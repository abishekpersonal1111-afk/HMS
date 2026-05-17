package com.hms.dao;

import com.hms.model.Patient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PatientDao implements GenericDao<Patient, Integer> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Patient save(Patient p) {
        entityManager.persist(p);
        return p;
    }

    @Override
    public Optional<Patient> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(Patient.class, id));
    }

    @Override
    public List<Patient> findAll() {
        return entityManager.createQuery("FROM Patient ORDER BY name", Patient.class)
                .getResultList();
    }

    @Override
    public Patient update(Patient p) {
        return entityManager.merge(p);
    }

    @Override
    public void delete(Integer id) {
        Patient p = entityManager.find(Patient.class, id);
        if (p != null) entityManager.remove(p);
    }

    public List<Patient> search(String keyword) {
        return entityManager.createQuery("FROM Patient WHERE LOWER(name) LIKE :kw OR contactNumber LIKE :kw", Patient.class)
                .setParameter("kw", "%" + keyword.toLowerCase() + "%")
                .getResultList();
    }
}
