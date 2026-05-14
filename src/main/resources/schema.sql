-- ============================================================
-- HMS Database Schema
-- Run against MySQL: CREATE DATABASE hms; USE hms;
-- ============================================================

CREATE DATABASE IF NOT EXISTS hms;
USE hms;

-- ─── Patient ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Patient (
    patientId       INT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(100)  NOT NULL,
    dateOfBirth     DATE,
    gender          VARCHAR(10),
    contactNumber   VARCHAR(15),
    address         VARCHAR(255),
    medicalHistory  TEXT
);

-- ─── Doctor ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Doctor (
    doctorId              INT PRIMARY KEY AUTO_INCREMENT,
    name                  VARCHAR(100)  NOT NULL,
    specialization        VARCHAR(100),
    contactNumber         VARCHAR(15),
    availabilitySchedule  TEXT
);

-- ─── User (RBAC) ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS User (
    userId    INT PRIMARY KEY AUTO_INCREMENT,
    username  VARCHAR(50)  UNIQUE NOT NULL,
    password  VARCHAR(255) NOT NULL,
    role      ENUM('ADMIN','PATIENT','DOCTOR') NOT NULL DEFAULT 'PATIENT'
);

-- ─── Appointment ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Appointment (
    appointmentId   INT PRIMARY KEY AUTO_INCREMENT,
    patientId       INT  NOT NULL,
    doctorId        INT  NOT NULL,
    appointmentDate DATE NOT NULL,
    timeSlot        VARCHAR(20),
    status          ENUM('CONFIRMED','CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    FOREIGN KEY (patientId) REFERENCES Patient(patientId) ON DELETE CASCADE,
    FOREIGN KEY (doctorId)  REFERENCES Doctor(doctorId)   ON DELETE CASCADE
);

-- ─── Bill ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Bill (
    billId          INT PRIMARY KEY AUTO_INCREMENT,
    patientId       INT           NOT NULL,
    totalAmount     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    paymentStatus   ENUM('PAID','UNPAID') NOT NULL DEFAULT 'UNPAID',
    billDate        DATE,
    FOREIGN KEY (patientId) REFERENCES Patient(patientId) ON DELETE CASCADE
);

-- ─── Prescription ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Prescription (
    prescriptionId  INT PRIMARY KEY AUTO_INCREMENT,
    patientId       INT  NOT NULL,
    doctorId        INT  NOT NULL,
    dateIssued      DATE NOT NULL,
    diagnosis       TEXT,
    FOREIGN KEY (patientId) REFERENCES Patient(patientId) ON DELETE CASCADE,
    FOREIGN KEY (doctorId)  REFERENCES Doctor(doctorId)   ON DELETE CASCADE
);

-- ─── Medicine ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Medicine (
    medicineId      INT PRIMARY KEY AUTO_INCREMENT,
    prescriptionId  INT          NOT NULL,
    medicineName    VARCHAR(100) NOT NULL,
    dosage          VARCHAR(50),
    frequency       VARCHAR(50),
    duration        VARCHAR(50),
    FOREIGN KEY (prescriptionId) REFERENCES Prescription(prescriptionId) ON DELETE CASCADE
);

-- ─── Default Admin Account (password: admin123) ──────────────
INSERT IGNORE INTO User (username, password, role)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN');

-- ─── Sample Doctors ───────────────────────────────────────────
INSERT IGNORE INTO Doctor (name, specialization, contactNumber, availabilitySchedule) VALUES
('Dr. Ayesha Khan',   'Cardiologist',   '9876543210', 'Mon-Fri 09:00-13:00'),
('Dr. Ramesh Gupta',  'Orthopedist',    '9123456780', 'Tue-Sat 14:00-18:00'),
('Dr. Priya Sharma',  'Pediatrician',   '9988776655', 'Mon-Wed 10:00-14:00'),
('Dr. Ali Hassan',    'Neurologist',    '9001122334', 'Thu-Sat 09:00-12:00'),
('Dr. Meera Joshi',   'Dermatologist',  '9776655443', 'Mon-Fri 11:00-15:00');
