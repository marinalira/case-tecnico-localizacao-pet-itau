package br.com.itaucase.localizacao_pet.application.service;

import br.com.itaucase.localizacao_pet.domain.dto.PetLocationRequest;
import br.com.itaucase.localizacao_pet.domain.dto.PetLocationResponse;
import br.com.itaucase.localizacao_pet.domain.dto.PositionStackResponse;
import br.com.itaucase.localizacao_pet.domain.entity.PetLocation;
import br.com.itaucase.localizacao_pet.domain.repository.PetLocationRepository;
import br.com.itaucase.localizacao_pet.infrastructure.client.PositionStackClient;
import br.com.itaucase.localizacao_pet.infrastructure.metrics.PetLocationMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PetLocationServiceTest {

    @Mock
    private PetLocationRepository petLocationRepository;

    @Mock
    private PositionStackClient positionStackClient;

    @Mock
    private PetLocationMetrics metrics;

    @InjectMocks
    private PetLocationService petLocationService;

    private PetLocationRequest petLocationRequest;
    private PetLocation petLocation;
    private PositionStackResponse positionStackResponse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(petLocationService, "positionStackApiKey", "test-api-key");

        petLocationRequest = PetLocationRequest.builder()
                .sensorId("SENSOR123")
                .latitude(-23.5505)
                .longitude(-46.6333)
                .timestamp(LocalDateTime.now())
                .build();

        petLocation = PetLocation.builder()
                .id(1L)
                .sensorId("SENSOR123")
                .latitude(-23.5505)
                .longitude(-46.6333)
                .timestamp(LocalDateTime.now())
                .isLocationResolved(false)
                .createdAt(LocalDateTime.now())
                .build();

        PositionStackResponse.PositionStackData data = PositionStackResponse.PositionStackData.builder()
                .country("Brasil")
                .administrative_area("São Paulo")
                .locality("São Paulo")
                .neighbourhood("Centro")
                .street("Avenida Paulista")
                .build();

        positionStackResponse = PositionStackResponse.builder()
                .data(Collections.singletonList(data))
                .build();
    }

    @Test
    void savePetLocation_ShouldReturnPetLocationResponse_WhenValidRequest() {
        // Given
        when(petLocationRepository.save(any(PetLocation.class))).thenReturn(petLocation);
        when(positionStackClient.getReverseGeocoding(anyString(), anyString(), anyInt(), anyString()))
                .thenReturn(positionStackResponse);

        // When
        PetLocationResponse response = petLocationService.savePetLocation(petLocationRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSensorId()).isEqualTo(petLocationRequest.getSensorId());
        assertThat(response.getLatitude()).isEqualTo(petLocationRequest.getLatitude());
        assertThat(response.getLongitude()).isEqualTo(petLocationRequest.getLongitude());

        verify(petLocationRepository, times(2)).save(any(PetLocation.class));
        verify(metrics).incrementLocationsRegistered();
        verify(metrics).incrementLocationsResolved();
    }

    @Test
    void getLastPetLocation_ShouldReturnPetLocationResponse_WhenLocationExists() {
        // Given
        when(petLocationRepository.findLastLocationBySensorId("SENSOR123")).thenReturn(Optional.of(petLocation));

        // When
        PetLocationResponse response = petLocationService.getLastPetLocation("SENSOR123");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSensorId()).isEqualTo("SENSOR123");

        verify(petLocationRepository).findLastLocationBySensorId("SENSOR123");
    }

    @Test
    void getLastPetLocation_ShouldThrowException_WhenLocationNotFound() {
        // Given
        when(petLocationRepository.findLastLocationBySensorId("SENSOR123")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> petLocationService.getLastPetLocation("SENSOR123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Pet location not found for sensor: SENSOR123");

        verify(petLocationRepository).findLastLocationBySensorId("SENSOR123");
    }

    @Test
    void getLastPetLocation_ShouldNotResolveLocation_WhenAlreadyResolved() {
        // Given
        petLocation.setIsLocationResolved(true);
        petLocation.setCountry("Brasil");
        petLocation.setState("São Paulo");
        when(petLocationRepository.findLastLocationBySensorId("SENSOR123")).thenReturn(Optional.of(petLocation));

        // When
        PetLocationResponse response = petLocationService.getLastPetLocation("SENSOR123");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getIsLocationResolved()).isTrue();
        assertThat(response.getCountry()).isEqualTo("Brasil");

        verify(petLocationRepository).findLastLocationBySensorId("SENSOR123");
        // Verifica que NÃO tentou resolver novamente
        verify(positionStackClient, never()).getReverseGeocoding(anyString(), anyString(), anyInt(), anyString());
    }

    @Test
    void savePetLocation_ShouldHandleApiError_Gracefully() {
        // Given
        when(petLocationRepository.save(any(PetLocation.class))).thenReturn(petLocation);
        when(positionStackClient.getReverseGeocoding(anyString(), anyString(), anyInt(), anyString()))
                .thenThrow(new RuntimeException("API Error"));

        // When
        PetLocationResponse response = petLocationService.savePetLocation(petLocationRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSensorId()).isEqualTo(petLocationRequest.getSensorId());

        verify(petLocationRepository).save(any(PetLocation.class));
        verify(metrics).incrementLocationsRegistered();
        verify(metrics).incrementApiErrors();
    }
}

