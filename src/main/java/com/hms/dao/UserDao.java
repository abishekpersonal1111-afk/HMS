package com.hms.dao;

import com.hms.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDao implements GenericDao<User, Integer> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public User save(User u) {
        entityManager.persist(u);
        return u;
    }

    @Override
    public Optional<User> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(User.class, id));
    }

    @Override
    public List<User> findAll() {
        return entityManager.createQuery("FROM User ORDER BY username", User.class)
                .getResultList();
    }

    @Override
    public User update(User u) {
        return entityManager.merge(u);
    }

    @Override
    public void delete(Integer id) {
        User u = entityManager.find(User.class, id);
        if (u != null) entityManager.remove(u);
    }

    public Optional<User> findByUsername(String username) {
        List<User> list = entityManager.createQuery("FROM User WHERE username = :un", User.class)
                .setParameter("un", username)
                .getResultList();
        return list.stream().findFirst();
    }
}

