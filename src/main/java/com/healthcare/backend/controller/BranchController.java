package com.healthcare.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthcare.backend.dto.request.BranchRequestDto;
import com.healthcare.backend.dto.response.BranchResponseDto;
import com.healthcare.backend.service.IBranchService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {
    private final IBranchService branchService;
    @GetMapping
    public ResponseEntity<List<BranchResponseDto>> getAll() {
        return ResponseEntity.ok(branchService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BranchResponseDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(branchService.getbyId(id));
    }

    @PostMapping
    public ResponseEntity<BranchResponseDto> create(@Valid @RequestBody BranchRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(branchService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BranchResponseDto> update(@PathVariable Integer id,
                                                  @Valid @RequestBody BranchRequestDto request) {
        return ResponseEntity.ok(branchService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        branchService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
