package com.autohub.web.dto.vehicle;

import com.autohub.domain.model.enums.VehicleCategory;

import java.time.LocalDateTime;
import java.util.UUID;

public record VehicleResponse(
        UUID id,
        String make,
        String model,
        Short year,
        VehicleCategory category,
        String color,
        String plate,
        String description,
        String coverUrl,
        LocalDateTime createdAt
) {}
