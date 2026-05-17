package com.hms.dao;

import com.hms.model.Doctor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DoctorDao implements GenericDao<Doctor, Integer> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Doctor save(Doctor d) {
        entityManager.persist(d);
        return d;
    }

    @Override
    public Optional<Doctor> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(Doctor.class, id));
    }

    @Override
    public List<Doctor> findAll() {
        return entityManager.createQuery("FROM Doctor ORDER BY name", Doctor.class)
                .getResultList();
    }

    @Override
    public Doctor update(Doctor d) {
        return entityManager.merge(d);
    }

    @Override
    public void delete(Integer id) {
        Doctor d = entityManager.find(Doctor.class, id);
        if (d != null)
            entityManager.remove(d);
    }

    public Optional<Doctor> findByUserId(Integer userId) {
        return entityManager.createQuery("FROM Doctor WHERE user.userId = :uid", Doctor.class)
                .setParameter("uid", userId)
                .getResultStream()
                .findFirst();
    }

    public List<Doctor> findBySpecialization(String spec) {
        return entityManager.createQuery("FROM Doctor WHERE LOWER(specialization) LIKE :s", Doctor.class)
                .setParameter("s", "%" + spec.toLowerCase() + "%")
                .getResultList();
    }
}
