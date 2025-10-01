package br.com.itaucase.localizacao_pet.application.service;

import br.com.itaucase.localizacao_pet.domain.dto.PetLocationRequest;
import br.com.itaucase.localizacao_pet.domain.dto.PetLocationResponse;
import br.com.itaucase.localizacao_pet.domain.dto.PositionStackResponse;
import br.com.itaucase.localizacao_pet.domain.entity.PetLocation;
import br.com.itaucase.localizacao_pet.domain.repository.PetLocationRepository;
import br.com.itaucase.localizacao_pet.infrastructure.client.PositionStackClient;
import br.com.itaucase.localizacao_pet.infrastructure.metrics.PetLocationMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetLocationService {

    private final PetLocationRepository petLocationRepository;
    private final PositionStackClient positionStackClient;
    private final PetLocationMetrics metrics;

    @Value("${positionstack.api.key}")
    private String positionStackApiKey;

    @Transactional
    public PetLocationResponse savePetLocation(PetLocationRequest request) {
        log.info("Saving pet location for sensor: {}", request.getSensorId());

        PetLocation petLocation = PetLocation.builder()
                .sensorId(request.getSensorId())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .timestamp(request.getTimestamp())
                .isLocationResolved(false)
                .build();

        PetLocation savedLocation = petLocationRepository.save(petLocation);
        metrics.incrementLocationsRegistered();

        resolveLocation(savedLocation);

        log.info("Pet location saved with ID: {}", savedLocation.getId());

        return mapToResponse(savedLocation);
    }

    @Transactional
    public PetLocationResponse getLastPetLocation(String sensorId) {
        log.info("Getting last location for sensor: {}", sensorId);

        Optional<PetLocation> locationOpt = petLocationRepository.findLastLocationBySensorId(sensorId);

        if (locationOpt.isEmpty()) {
            log.warn("No location found for sensor: {}", sensorId);
            throw new IllegalArgumentException("Pet location not found for sensor: " + sensorId);
        }

        PetLocation location = locationOpt.get();

        if (Boolean.FALSE.equals(location.getIsLocationResolved())) {
            resolveLocation(location);
        }

        return mapToResponse(location);
    }

    private void resolveLocation(PetLocation location) {
        try {
            log.info("Attempting to resolve location for coordinates: {}, {}", location.getLatitude(), location.getLongitude());

            String coordinates = location.getLatitude() + "," + location.getLongitude();
            log.debug("Calling PositionStack API with coordinates: {}", coordinates);

            PositionStackResponse response = positionStackClient.getReverseGeocoding(
                    positionStackApiKey,
                    coordinates,
                    1,
                    "json"
            );

            log.debug("PositionStack API response: {}", response);

            if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                PositionStackResponse.PositionStackData data = response.getData().get(0);

                location.setCountry(data.getCountry());
                location.setState(data.getAdministrative_area());
                location.setCity(data.getLocality());
                location.setNeighborhood(data.getNeighbourhood());
                location.setAddress(buildAddress(data));
                location.setIsLocationResolved(true);

                petLocationRepository.save(location);
                metrics.incrementLocationsResolved();

                log.info("Location resolved successfully - Country: {}, City: {}", data.getCountry(), data.getLocality());
            } else {
                log.warn("PositionStack API returned empty data for coordinates: {}, {}", location.getLatitude(), location.getLongitude());
                log.warn("This usually means: 1) API key is invalid, 2) Rate limit reached, or 3) API is down");
            }
        } catch (Exception e) {
            log.error("Error resolving location for coordinates {}, {}: {} - {}",
                    location.getLatitude(), location.getLongitude(),
                    e.getClass().getSimpleName(), e.getMessage());
            metrics.incrementApiErrors();
        }
    }

    private String buildAddress(PositionStackResponse.PositionStackData data) {
        StringBuilder address = new StringBuilder();

        if (data.getStreet() != null && !data.getStreet().isEmpty()) {
            address.append(data.getStreet());
        } else if (data.getName() != null && !data.getName().isEmpty()) {
            address.append(data.getName());
        }

        return address.toString().trim();
    }

    private PetLocationResponse mapToResponse(PetLocation location) {
        return PetLocationResponse.builder()
                .id(location.getId())
                .sensorId(location.getSensorId())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .timestamp(location.getTimestamp())
                .country(location.getCountry())
                .state(location.getState())
                .city(location.getCity())
                .neighborhood(location.getNeighborhood())
                .address(location.getAddress())
                .isLocationResolved(location.getIsLocationResolved())
                .createdAt(location.getCreatedAt())
                .build();
    }
}
