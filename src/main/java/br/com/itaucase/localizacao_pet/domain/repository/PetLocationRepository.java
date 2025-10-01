package br.com.itaucase.localizacao_pet.domain.repository;

import br.com.itaucase.localizacao_pet.domain.entity.PetLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PetLocationRepository extends JpaRepository<PetLocation, Long> {

    @Query("SELECT pl FROM PetLocation pl WHERE pl.sensorId = :sensorId ORDER BY pl.timestamp DESC LIMIT 1")
    Optional<PetLocation> findLastLocationBySensorId(@Param("sensorId") String sensorId);

}
