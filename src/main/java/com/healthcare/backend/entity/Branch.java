package com.healthcare.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Branch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BranchId")
    private Integer branchId;

    @Column(name = "BranchName", nullable = false, length = 255)
    private String branchName;

    @Column(name = "BranchAddress", length = 500)
    private String branchAddress;

    @Column(name = "BranchHotline", length = 20)
    private String branchHotline;
}