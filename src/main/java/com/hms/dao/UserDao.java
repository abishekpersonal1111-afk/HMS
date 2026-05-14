package com.hms.dao;

import com.hms.model.User;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDao implements GenericDao<User, Integer> {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public User save(User u) {
        sessionFactory.getCurrentSession().persist(u);
        return u;
    }

    @Override
    public Optional<User> findById(Integer id) {
        return Optional.ofNullable(sessionFactory.getCurrentSession().get(User.class, id));
    }

    @Override
    public List<User> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM User ORDER BY username", User.class)
                .getResultList();
    }

    @Override
    public User update(User u) {
        return (User) sessionFactory.getCurrentSession().merge(u);
    }

    @Override
    public void delete(Integer id) {
        User u = sessionFactory.getCurrentSession().get(User.class, id);
        if (u != null) sessionFactory.getCurrentSession().remove(u);
    }

    public Optional<User> findByUsername(String username) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM User WHERE username = :un", User.class)
                .setParameter("un", username)
                .uniqueResultOptional();
    }
}
