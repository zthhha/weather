package com.tlias.weather.city;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findTop20ByNameContainingIgnoreCaseOrderByNameAsc(String keyword);
    Optional<City> findByName(String name);
}
