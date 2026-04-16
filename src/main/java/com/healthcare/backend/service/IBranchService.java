package com.healthcare.backend.service;
import com.healthcare.backend.dto.request.*;
import com.healthcare.backend.dto.response.*;
import com.healthcare.backend.dto.request.BranchRequestDto;
import com.healthcare.backend.dto.response.BranchResponseDto;

import java.util.List;
public interface IBranchService {
    List<BranchResponseDto> getAll();
    BranchResponseDto getbyId(Integer id);
    BranchResponseDto create(BranchRequestDto requestDto);
    BranchResponseDto update(Integer id, BranchRequestDto requestDto);
    void delete (Integer id);

} 
