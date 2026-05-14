package com.hms.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "Patient")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer patientId;

    @Column(nullable = false, length = 100)
    private String name;

    private LocalDate dateOfBirth;

    @Column(length = 10)
    private String gender;

    @Column(length = 15)
    private String contactNumber;

    @Column(length = 255)
    private String address;

    @Column(columnDefinition = "TEXT")
    private String medicalHistory;
}
