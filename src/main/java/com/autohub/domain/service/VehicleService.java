package com.autohub.domain.service;

import com.autohub.domain.model.User;
import com.autohub.domain.model.Vehicle;
import com.autohub.domain.repository.UserRepository;
import com.autohub.domain.repository.VehicleRepository;
import com.autohub.shared.exception.exceptions.ResourceNotFoundException;
import com.autohub.web.dto.vehicle.VehicleRequest;
import com.autohub.web.dto.vehicle.VehicleResponse;
import com.autohub.web.mapper.VehicleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final VehicleMapper vehicleMapper;

    @Transactional
    public VehicleResponse create(VehicleRequest request) {
        User owner = currentUser();
        Vehicle vehicle = vehicleMapper.toEntity(request);
        vehicle.setOwner(owner);
        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional(readOnly = true)
    public Page<VehicleResponse> listMyVehicles(Pageable pageable) {
        return vehicleRepository
                .findByOwnerId(currentUser().getId(), pageable)
                .map(vehicleMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public VehicleResponse getById(UUID id) {
        return vehicleMapper.toResponse(findOwnedOrThrow(id));
    }

    @Transactional
    public VehicleResponse update(UUID id, VehicleRequest request) {
        Vehicle vehicle = findOwnedOrThrow(id);
        vehicleMapper.updateEntity(request, vehicle);
        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public void delete(UUID id) {
        vehicleRepository.delete(findOwnedOrThrow(id));
    }

    private Vehicle findOwnedOrThrow(UUID id) {
        UUID ownerId = currentUser().getId();
        return vehicleRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }
}
