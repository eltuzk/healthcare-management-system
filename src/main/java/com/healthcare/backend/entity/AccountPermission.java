package com.healthcare.backend.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "account_permission")
public class AccountPermission  {
    @EmbeddedId
    private AccountPermissionId accountPermissionId;

    @ManyToOne
    @MapsId("accountId")
    @JoinColumn(name = "account_Id")
    private Account account;

    @ManyToOne
    @MapsId("permissionId")
    @JoinColumn(name = "permission_Id")
    private Permission permission;

    public AccountPermission() {
    }

    public AccountPermission(AccountPermissionId accountPermissionId, Account account, Permission permission) {
        this.accountPermissionId = accountPermissionId;
        this.account = account;
        this.permission = permission;
    }

    public AccountPermissionId getAccountPermissionId() {
        return accountPermissionId;
    }

    public void setAccountPermissionId(AccountPermissionId accountPermissionId) {
        this.accountPermissionId = accountPermissionId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    
}
