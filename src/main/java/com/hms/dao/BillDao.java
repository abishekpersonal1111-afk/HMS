package com.hms.dao;

import com.hms.model.Bill;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BillDao implements GenericDao<Bill, Integer> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Bill save(Bill b) {
        entityManager.persist(b);
        return b;
    }

    @Override
    public Optional<Bill> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(Bill.class, id));
    }

    @Override
    public List<Bill> findAll() {
        return entityManager.createQuery("FROM Bill ORDER BY billDate DESC", Bill.class)
                .getResultList();
    }

    @Override
    public Bill update(Bill b) {
        return entityManager.merge(b);
    }

    @Override
    public void delete(Integer id) {
        Bill b = entityManager.find(Bill.class, id);
        if (b != null) entityManager.remove(b);
    }

    public List<Bill> findByPatient(Integer patientId) {
        return entityManager.createQuery("FROM Bill WHERE patient.patientId = :pid ORDER BY billDate DESC", Bill.class)
                .setParameter("pid", patientId)
                .getResultList();
    }

    public List<Bill> findByStatus(Bill.PaymentStatus status) {
        return entityManager.createQuery("FROM Bill WHERE paymentStatus = :s", Bill.class)
                .setParameter("s", status)
                .getResultList();
    }
}
