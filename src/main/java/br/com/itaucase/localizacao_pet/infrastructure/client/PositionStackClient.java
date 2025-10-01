package br.com.itaucase.localizacao_pet.infrastructure.client;

import br.com.itaucase.localizacao_pet.domain.dto.PositionStackResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "positionstack", url = "${positionstack.api.url}")
public interface PositionStackClient {

    @GetMapping("/reverse")
    PositionStackResponse getReverseGeocoding(
            @RequestParam("access_key") String accessKey,
            @RequestParam("query") String coordinates,
            @RequestParam(value = "limit", defaultValue = "1") int limit,
            @RequestParam(value = "output", defaultValue = "json") String output
    );
}
