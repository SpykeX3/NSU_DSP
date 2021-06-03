package ru.nsu.flights.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.flights.model.Route;

public interface RoutesRepository extends JpaRepository<Route, String> {
    List<Route> findRouteDTOSByArrivalAirport(String arrivalAirport);

    List<Route> findRouteDTOSByDepartureAirport(String departureAirport);

    List<Route> findRouteDTOSByDepartureAirportAndArrivalAirport(String departureAirport, String arrivalAirport);
}
