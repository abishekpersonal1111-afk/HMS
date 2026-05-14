package com.hms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Medicine")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer medicineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescriptionId", nullable = false)
    @JsonIgnore
    private Prescription prescription;

    @Column(nullable = false, length = 100)
    private String medicineName;

    @Column(length = 50)
    private String dosage;

    @Column(length = 50)
    private String frequency;   // e.g. "1-0-1"

    @Column(length = 50)
    private String duration;    // e.g. "7 days"
}
