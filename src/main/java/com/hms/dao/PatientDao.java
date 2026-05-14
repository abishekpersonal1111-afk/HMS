package com.hms.dao;

import com.hms.model.Patient;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class PatientDao implements GenericDao<Patient, Integer> {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public Patient save(Patient p) {
        sessionFactory.getCurrentSession().persist(p);
        return p;
    }

    @Override
    public Optional<Patient> findById(Integer id) {
        return Optional.ofNullable(sessionFactory.getCurrentSession().get(Patient.class, id));
    }

    @Override
    public List<Patient> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Patient ORDER BY name", Patient.class)
                .getResultList();
    }

    @Override
    public Patient update(Patient p) {
        return (Patient) sessionFactory.getCurrentSession().merge(p);
    }

    @Override
    public void delete(Integer id) {
        Patient p = sessionFactory.getCurrentSession().get(Patient.class, id);
        if (p != null) sessionFactory.getCurrentSession().remove(p);
    }

    public List<Patient> search(String keyword) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Patient WHERE LOWER(name) LIKE :kw OR contactNumber LIKE :kw", Patient.class)
                .setParameter("kw", "%" + keyword.toLowerCase() + "%")
                .getResultList();
    }
}
