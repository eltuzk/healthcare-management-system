package com.healthcare.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "MEDICINE")
@Getter
@Setter
@NoArgsConstructor
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicine_id")
    private Long medicineId;

    @Column(name = "medicine_name", nullable = false, unique = true, length = 200)
    private String medicineName;

    @Column(name = "active_ingredient", length = 200)
    private String activeIngredient;

    @Column(name = "unit", nullable = false, length = 50)
    private String unit;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Integer isActive = 1;

    @Column(name = "selling_price", nullable = false, precision = 15, scale = 2)
    private java.math.BigDecimal sellingPrice = java.math.BigDecimal.ZERO;
}