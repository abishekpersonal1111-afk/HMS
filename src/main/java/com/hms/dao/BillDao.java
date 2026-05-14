package com.hms.dao;

import com.hms.model.Bill;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class BillDao implements GenericDao<Bill, Integer> {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public Bill save(Bill b) {
        sessionFactory.getCurrentSession().persist(b);
        return b;
    }

    @Override
    public Optional<Bill> findById(Integer id) {
        return Optional.ofNullable(sessionFactory.getCurrentSession().get(Bill.class, id));
    }

    @Override
    public List<Bill> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Bill ORDER BY billDate DESC", Bill.class)
                .getResultList();
    }

    @Override
    public Bill update(Bill b) {
        return (Bill) sessionFactory.getCurrentSession().merge(b);
    }

    @Override
    public void delete(Integer id) {
        Bill b = sessionFactory.getCurrentSession().get(Bill.class, id);
        if (b != null) sessionFactory.getCurrentSession().remove(b);
    }

    public List<Bill> findByPatient(Integer patientId) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Bill WHERE patient.patientId = :pid ORDER BY billDate DESC", Bill.class)
                .setParameter("pid", patientId)
                .getResultList();
    }

    public List<Bill> findByStatus(Bill.PaymentStatus status) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Bill WHERE paymentStatus = :s", Bill.class)
                .setParameter("s", status)
                .getResultList();
    }
}
