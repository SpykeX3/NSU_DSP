package ru.nsu.flights.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.flights.model.Airport;

public interface AirportRepository extends JpaRepository<Airport, String> {
    List<Airport> findAirportsByCity(String city);
}
