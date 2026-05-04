package com.healthcare.backend.dto.response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BedResponse {

    private Long bedId;
    private BigDecimal price;
    private String status;
    private Long roomId;
}

