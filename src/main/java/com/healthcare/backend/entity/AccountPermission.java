package com.healthcare.backend.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "account_permission")
@Getter
@Setter
public class AccountPermission {
    @EmbeddedId
    private AccountPermissionId accountPermissionId;

    @ManyToOne
    @MapsId("accountId")
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @MapsId("permissionId")
    @JoinColumn(name = "permission_id")
    private Permission permission;

    public AccountPermission() {
    }

    public AccountPermission(AccountPermissionId accountPermissionId, Account account, Permission permission) {
        this.accountPermissionId = accountPermissionId;
        this.account = account;
        this.permission = permission;
    }
}
