package br.com.itaucase.localizacao_pet.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PetLocationMetrics {

    private final Counter locationsRegisteredCounter;
    private final Counter locationsResolvedCounter;
    private final Counter apiErrorsCounter;

    public PetLocationMetrics(MeterRegistry meterRegistry) {
        this.locationsRegisteredCounter = Counter.builder("pet.locations.registered")
                .description("Total de localizações registradas")
                .register(meterRegistry);

        this.locationsResolvedCounter = Counter.builder("pet.locations.resolved")
                .description("Total de localizações resolvidas com sucesso")
                .register(meterRegistry);

        this.apiErrorsCounter = Counter.builder("pet.api.errors")
                .description("Total de erros na API externa")
                .register(meterRegistry);

        log.info("Métricas inicializadas com sucesso");
    }

    public void incrementLocationsRegistered() {
        locationsRegisteredCounter.increment();
    }

    public void incrementLocationsResolved() {
        locationsResolvedCounter.increment();
    }

    public void incrementApiErrors() {
        apiErrorsCounter.increment();
    }
}

