package com.hms.service;

import com.hms.dao.UserDao;
import com.hms.model.User;
import com.hms.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserDao userDao;

    public User register(User user) {
        if (userDao.findByUsername(user.getUsername()).isPresent())
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        user.setPassword(PasswordUtil.hash(user.getPassword()));
        return userDao.save(user);
    }

    public User login(String username, String rawPassword) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!PasswordUtil.verify(rawPassword, user.getPassword()))
            throw new RuntimeException("Invalid credentials");
        return user;
    }

    public User getUser(int id) {
        return userDao.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public User updateUser(User user) {
        getUser(user.getUserId());
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$"))
            user.setPassword(PasswordUtil.hash(user.getPassword()));
        return userDao.update(user);
    }

    public void deleteUser(int id) {
        userDao.delete(id);
    }
}
