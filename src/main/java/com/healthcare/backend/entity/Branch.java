package com.healthcare.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "BRANCH")
@Getter
@Setter
@NoArgsConstructor
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "branch_name", unique = true, nullable = false, length = 200)
    private String branchName;

    @Column(name = "branch_address", nullable = false, length = 500)
    private String branchAddress;

    @Column(name = "branch_hotline", length = 20)
    private String branchHotline;
}
