package org.gagan.florasense.Repositary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.gagan.florasense.Entity.PlantInfo;
import org.gagan.florasense.Enum.CareLevel;
import org.gagan.florasense.Enum.PlantCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlantInfoRepositary
        extends JpaRepository<PlantInfo, Integer>,
        JpaSpecificationExecutor<PlantInfo> {

    Optional<PlantInfo> findByPlantNameIgnoreCase(String plantName);

    @Query("""
        SELECT p FROM PlantInfo p
        WHERE
            p.isActive = true AND /*Only Active Plant will return*/
            (
                :search = ''
                OR LOWER(p.plantName) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(p.scientificName) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            AND (:category IS NULL OR p.plantCategory = :category)
            AND (:careLevel IS NULL OR p.careLevel = :careLevel)
            AND (:petSafe IS NULL OR p.isPetSafe = :petSafe)
    """)
    Page<PlantInfo> searchPlants(String search,
                                 PlantCategory category,
                                 CareLevel careLevel,
                                 Boolean petSafe,
                                 Pageable pageable
    );

    //find plant details by  using plantId
    Optional<PlantInfo> findByPlantId(Integer plantId);

    boolean existsByPlantNameIgnoreCase(String plantName);

    List<PlantInfo> findByIsActiveTrue();

    long countByIsActiveTrue();

    Optional<PlantInfo> findByPlantIdAndIsActiveTrue(Integer plantId);

    //This searches uploaded plant catalogue by scientific name.
    Optional<PlantInfo> findFirstByScientificNameIgnoreCase(String scientificName);

    Optional<PlantInfo> findFirstByPlantNameIgnoreCase(String plantName);
}
