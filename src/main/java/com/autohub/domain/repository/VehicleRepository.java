package com.autohub.domain.repository;

import com.autohub.domain.model.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    Page<Vehicle> findByOwnerId(UUID ownerId, Pageable pageable);
    Optional<Vehicle> findByIdAndOwnerId(UUID id, UUID ownerId);
}
