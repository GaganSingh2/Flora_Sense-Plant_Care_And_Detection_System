package org.gagan.florasense.Repositary;

import org.gagan.florasense.Entity.FavoritePlant;
import org.gagan.florasense.Entity.PlantInfo;
import org.gagan.florasense.Entity.User;
import org.gagan.florasense.Enum.PlantCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoritePlantRepositary extends JpaRepository<FavoritePlant, Integer> {
    long count();

    Optional<FavoritePlant> findByUserAndPlantInfo(User user, PlantInfo plantInfo);

    @Query("""
        SELECT f
        FROM FavoritePlant f
        JOIN f.plantInfo p
        WHERE f.user = :user
            AND p.isActive = true
            AND (
                :search = ''
                OR LOWER(p.plantName) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(p.scientificName) LIKE LOWER(CONCAT('%', :search, '%'))
                )
            AND (:indoor IS NULL OR p.isIndoor = :indoor)
            AND (:category IS NULL OR p.plantCategory = :category)
    """)
    Page<FavoritePlant> searchMyFavoritesPlants(
            User user, String search,
            Boolean indoor, PlantCategory category,
            Pageable pageable
    );

    long countByUser(User user);
}
