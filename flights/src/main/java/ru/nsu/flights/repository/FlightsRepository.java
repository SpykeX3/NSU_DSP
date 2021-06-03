package ru.nsu.flights.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.flights.model.Flight;

public interface FlightsRepository extends JpaRepository<Flight, Integer> {
    Flight findFirstByFlightNo(String flightNo);
}
