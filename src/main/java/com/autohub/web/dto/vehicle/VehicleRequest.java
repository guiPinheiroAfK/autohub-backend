package com.autohub.web.dto.vehicle;

import com.autohub.domain.model.enums.VehicleCategory;
import jakarta.validation.constraints.*;

public record VehicleRequest(
        @NotBlank @Size(max = 50) String make,
        @NotBlank @Size(max = 50) String model,
        @NotNull @Min(1886) @Max(2100) Short year,
        @NotNull VehicleCategory category,
        @Size(max = 30) String color,
        @Size(max = 20) String plate,
        @Size(max = 2000) String description,
        String coverUrl
) {}
