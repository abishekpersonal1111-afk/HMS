package com.hms.service;

import com.hms.dao.DoctorDao;
import com.hms.dao.PatientDao;
import com.hms.dao.UserDao;
import com.hms.model.Doctor;
import com.hms.model.Patient;
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

    @Autowired
    private DoctorDao doctorDao;

    @Autowired
    private PatientDao patientDao;

    public User register(User user) {
        if (userDao.findByUsername(user.getUsername()).isPresent())
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        user.setPassword(PasswordUtil.hash(user.getPassword()));
        // If it's the first user or an admin, auto-approve (optional strategy)
        // For now, let's say ADMIN is auto-approved, others need approval
        if (user.getRole() == User.Role.ADMIN) {
            user.setApproved(true);
        } else {
            user.setApproved(false);
        }
        User savedUser = userDao.save(user);

        // If registering as DOCTOR, auto-create a Doctor record linked to this user
        if (savedUser.getRole() == User.Role.DOCTOR) {
            Doctor doctor = new Doctor();
            doctor.setUser(savedUser);
            doctor.setName(savedUser.getUsername()); // Default to username, can be updated later
            doctorDao.save(doctor);
        }

        // If registering as PATIENT, auto-create a Patient record linked to this user
        if (savedUser.getRole() == User.Role.PATIENT) {
            Patient patient = new Patient();
            patient.setUser(savedUser);
            patient.setName(savedUser.getUsername());
            patientDao.save(patient);
        }

        return savedUser;
    }

    public User login(String username, String rawPassword) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!PasswordUtil.verify(rawPassword, user.getPassword()))
            throw new RuntimeException("Invalid credentials");
        if (!user.isApproved())
            throw new RuntimeException("User account not approved by admin");
        return user;
    }

    public void ensureAdminApproved() {
        userDao.findByUsername("admin").ifPresent(admin -> {
            if (!admin.isApproved()) {
                admin.setApproved(true);
                userDao.update(admin);
                System.out.println("Default admin user auto-approved in database.");
            }
        });
    }

    public User approveUser(int id) {
        User user = getUser(id);
        user.setApproved(true);
        User approvedUser = userDao.update(user);
        if (approvedUser.getRole() == User.Role.DOCTOR) {
            doctorDao.findByUserId(approvedUser.getUserId())
                    .orElseGet(() -> {
                        Doctor doctor = new Doctor();
                        doctor.setUser(approvedUser);
                        doctor.setName(approvedUser.getUsername());
                        return doctorDao.save(doctor);
                    });
        }
        if (approvedUser.getRole() == User.Role.PATIENT) {
            patientDao.findByUserId(approvedUser.getUserId())
                    .orElseGet(() -> {
                        Patient patient = new Patient();
                        patient.setUser(approvedUser);
                        patient.setName(approvedUser.getUsername());
                        return patientDao.save(patient);
                    });
        }
        return approvedUser;
    }

    public User getUser(int id) {
        return userDao.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public List<User> getPendingUsers() {
        return userDao.findAll().stream()
                .filter(u -> !u.isApproved())
                .toList();
    }

    public User updateUser(User user) {
        User existing = getUser(user.getUserId());
        if (user.getPassword() != null && !user.getPassword().isEmpty() && !user.getPassword().startsWith("$2a$"))
            user.setPassword(PasswordUtil.hash(user.getPassword()));
        else
            user.setPassword(existing.getPassword());

        return userDao.update(user);
    }

    public void deleteUser(int id) {
        userDao.delete(id);
    }
}
