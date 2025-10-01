package br.com.itaucase.localizacao_pet.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionStackResponse {

    private List<PositionStackData> data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionStackData {
        
        private Double latitude;
        private Double longitude;
        
        @JsonProperty("type")
        private String type;
        
        private String name;
        
        @JsonProperty("number")
        private String number;
        
        private String postal_code;
        private String street;
        private String confidence;
        private String region;
        private String region_code;
        private String county;
        private String locality;
        private String administrative_area;
        private String neighbourhood;
        private String country;
        private String country_code;
        private String continent;
        private String label;
    }
}
