package br.com.itaucase.localizacao_pet.integration;

import br.com.itaucase.localizacao_pet.domain.dto.PetLocationRequest;
import br.com.itaucase.localizacao_pet.domain.entity.PetLocation;
import br.com.itaucase.localizacao_pet.domain.repository.PetLocationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PetLocationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PetLocationRepository petLocationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        petLocationRepository.deleteAll();
    }

    @Test
    void deveRegistrarLocalizacaoDePet_ComSucesso() throws Exception {
        // Given
        PetLocationRequest request = PetLocationRequest.builder()
                .sensorId("SENSOR001")
                .latitude(-23.5505)
                .longitude(-46.6333)
                .timestamp(LocalDateTime.now())
                .build();

        // When
        mockMvc.perform(post("/api/v1/pet-locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // Then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sensorId").value("SENSOR001"))
                .andExpect(jsonPath("$.latitude").value(-23.5505))
                .andExpect(jsonPath("$.longitude").value(-46.6333))
                .andExpect(jsonPath("$.id").exists());

        assertThat(petLocationRepository.count()).isEqualTo(1);
        PetLocation savedLocation = petLocationRepository.findAll().get(0);
        assertThat(savedLocation.getSensorId()).isEqualTo("SENSOR001");
    }

    @Test
    void deveBuscarUltimaLocalizacao_QuandoExistir() throws Exception {
        // Given
        PetLocation location = PetLocation.builder()
                .sensorId("SENSOR002")
                .latitude(-23.5505)
                .longitude(-46.6333)
                .timestamp(LocalDateTime.now())
                .isLocationResolved(false)
                .build();
        petLocationRepository.save(location);

        // When & Then
        mockMvc.perform(get("/api/v1/pet-locations/sensor/SENSOR002/last"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sensorId").value("SENSOR002"))
                .andExpect(jsonPath("$.latitude").value(-23.5505))
                .andExpect(jsonPath("$.longitude").value(-46.6333));
    }

    @Test
    void deveRetornarErro_QuandoLocalizacaoNaoExistir() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/pet-locations/sensor/SENSOR_INEXISTENTE/last"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deveValidarDadosDeEntrada_QuandoInvalidos() throws Exception {
        // Given
        PetLocationRequest requestInvalido = PetLocationRequest.builder()
                .sensorId("") // Sensor ID vazio - inválido
                .latitude(100.0) // Latitude inválida (deve estar entre -90 e 90)
                .longitude(-46.6333)
                .timestamp(LocalDateTime.now())
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/pet-locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());

        assertThat(petLocationRepository.count()).isZero();
    }

    @Test
    void deveRegistrarMultiplasLocalizacoes_DoMesmoPet() throws Exception {
        // Given
        PetLocationRequest request1 = PetLocationRequest.builder()
                .sensorId("SENSOR003")
                .latitude(-23.5505)
                .longitude(-46.6333)
                .timestamp(LocalDateTime.now().minusHours(1))
                .build();

        PetLocationRequest request2 = PetLocationRequest.builder()
                .sensorId("SENSOR003")
                .latitude(-23.5515)
                .longitude(-46.6343)
                .timestamp(LocalDateTime.now())
                .build();

        // When
        mockMvc.perform(post("/api/v1/pet-locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/pet-locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        // Then
        assertThat(petLocationRepository.count()).isEqualTo(2);

        mockMvc.perform(get("/api/v1/pet-locations/sensor/SENSOR003/last"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(-23.5515))
                .andExpect(jsonPath("$.longitude").value(-46.6343));
    }

    @Test
    void deveVerificarHealthCheck() throws Exception {
        // When & Then
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void deveRetornarInformacoesMetricas() throws Exception {
        // When & Then
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").isArray());
    }
}

