package com.healthcare.backend.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class AccountPermissionId implements Serializable {

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "permission_id")
    private Long permissionId;

    public AccountPermissionId(Long accountId, Long permissionId) {
        this.accountId = accountId;
        this.permissionId = permissionId;
    }
}
