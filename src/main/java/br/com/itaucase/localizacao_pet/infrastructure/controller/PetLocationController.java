package br.com.itaucase.localizacao_pet.infrastructure.controller;

import br.com.itaucase.localizacao_pet.application.service.PetLocationService;
import br.com.itaucase.localizacao_pet.domain.dto.PetLocationRequest;
import br.com.itaucase.localizacao_pet.domain.dto.PetLocationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/pet-locations")
@RequiredArgsConstructor
public class PetLocationController {

    private final PetLocationService petLocationService;

    @PostMapping
    public ResponseEntity<PetLocationResponse> registerPetLocation(@Valid @RequestBody PetLocationRequest request) {
        log.info("Received request to register pet location for sensor: {}", request.getSensorId());
        PetLocationResponse response = petLocationService.savePetLocation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/sensor/{sensorId}/last")
    public ResponseEntity<PetLocationResponse> getLastPetLocation(@PathVariable String sensorId) {
        log.info("Received request to get last location for sensor: {}", sensorId);
        PetLocationResponse response = petLocationService.getLastPetLocation(sensorId);
        return ResponseEntity.ok(response);
    }
}
