package com.healthcare.backend.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalServiceRequestItemId implements Serializable {

    private Long medServiceRequestId;
    private Long medServiceId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicalServiceRequestItemId that = (MedicalServiceRequestItemId) o;
        return Objects.equals(medServiceRequestId, that.medServiceRequestId) &&
                Objects.equals(medServiceId, that.medServiceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medServiceRequestId, medServiceId);
    }
}
