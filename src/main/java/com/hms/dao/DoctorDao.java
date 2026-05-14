package com.hms.dao;

import com.hms.model.Doctor;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class DoctorDao implements GenericDao<Doctor, Integer> {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public Doctor save(Doctor d) {
        sessionFactory.getCurrentSession().persist(d);
        return d;
    }

    @Override
    public Optional<Doctor> findById(Integer id) {
        return Optional.ofNullable(sessionFactory.getCurrentSession().get(Doctor.class, id));
    }

    @Override
    public List<Doctor> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Doctor ORDER BY name", Doctor.class)
                .getResultList();
    }

    @Override
    public Doctor update(Doctor d) {
        return (Doctor) sessionFactory.getCurrentSession().merge(d);
    }

    @Override
    public void delete(Integer id) {
        Doctor d = sessionFactory.getCurrentSession().get(Doctor.class, id);
        if (d != null) sessionFactory.getCurrentSession().remove(d);
    }

    public List<Doctor> findBySpecialization(String spec) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Doctor WHERE LOWER(specialization) LIKE :s", Doctor.class)
                .setParameter("s", "%" + spec.toLowerCase() + "%")
                .getResultList();
    }
}
