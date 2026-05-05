package com.healthcare.backend.service;

import java.util.List;

import com.healthcare.backend.dto.request.AccountPermissionRequest;
import com.healthcare.backend.dto.response.AccountPermissionResponse;

public interface AccountPermissionService {
    AccountPermissionResponse assign(AccountPermissionRequest request);

    void revoke(AccountPermissionRequest request);

    List<AccountPermissionResponse> getByAccountId(Long accountId);
}
