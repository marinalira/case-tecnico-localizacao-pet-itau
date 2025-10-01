package br.com.itaucase.localizacao_pet.infrastructure.controller;

import br.com.itaucase.localizacao_pet.application.service.PetLocationService;
import br.com.itaucase.localizacao_pet.domain.dto.PetLocationRequest;
import br.com.itaucase.localizacao_pet.domain.dto.PetLocationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PetLocationController.class)
class PetLocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private PetLocationService petLocationService;

    @Autowired
    private ObjectMapper objectMapper;

    private PetLocationRequest petLocationRequest;
    private PetLocationResponse petLocationResponse;

    @BeforeEach
    void setUp() {
        petLocationRequest = PetLocationRequest.builder()
                .sensorId("SENSOR123")
                .latitude(-23.5505)
                .longitude(-46.6333)
                .timestamp(LocalDateTime.now())
                .build();

        petLocationResponse = PetLocationResponse.builder()
                .id(1L)
                .sensorId("SENSOR123")
                .latitude(-23.5505)
                .longitude(-46.6333)
                .timestamp(LocalDateTime.now())
                .country("Brasil")
                .state("São Paulo")
                .city("São Paulo")
                .neighborhood("Centro")
                .address("Avenida Paulista")
                .isLocationResolved(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void registerPetLocation_ShouldReturnCreated_WhenValidRequest() throws Exception {
        // Given
        when(petLocationService.savePetLocation(any(PetLocationRequest.class)))
                .thenReturn(petLocationResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/pet-locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(petLocationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sensorId").value("SENSOR123"))
                .andExpect(jsonPath("$.latitude").value(-23.5505))
                .andExpect(jsonPath("$.longitude").value(-46.6333))
                .andExpect(jsonPath("$.country").value("Brasil"))
                .andExpect(jsonPath("$.isLocationResolved").value(true));
    }

    @Test
    void registerPetLocation_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Given
        PetLocationRequest invalidRequest = PetLocationRequest.builder()
                .sensorId("")
                .latitude(100.0)
                .longitude(-46.6333)
                .timestamp(LocalDateTime.now())
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/pet-locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getLastPetLocation_ShouldReturnOk_WhenLocationExists() throws Exception {
        // Given
        when(petLocationService.getLastPetLocation("SENSOR123"))
                .thenReturn(petLocationResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/pet-locations/sensor/SENSOR123/last"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sensorId").value("SENSOR123"))
                .andExpect(jsonPath("$.country").value("Brasil"))
                .andExpect(jsonPath("$.isLocationResolved").value(true));
    }

    @Test
    void getLastPetLocation_ShouldReturnNotFound_WhenLocationDoesNotExist() throws Exception {
        // Given
        when(petLocationService.getLastPetLocation("SENSOR_INEXISTENTE"))
                .thenThrow(new RuntimeException("Pet location not found for sensor: SENSOR_INEXISTENTE"));

        // When & Then
        mockMvc.perform(get("/api/v1/pet-locations/sensor/SENSOR_INEXISTENTE/last"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}

