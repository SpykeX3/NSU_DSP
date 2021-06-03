package ru.nsu.flights.controller;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.flights.model.Airport;
import ru.nsu.flights.repository.AirportRepository;

@RestController
public class LocationsController {

    private final AirportRepository airportRepository;

    @Autowired
    LocationsController(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    @GetMapping(value = "airports", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Airport> getAirports(@RequestParam(required = false, name = "city") String city) {
        if (city == null) {
            return airportRepository.findAll();
        }
        return airportRepository.findAirportsByCity(StringUtils.capitalize(city.toLowerCase(Locale.ROOT)));
    }

    @GetMapping(value = "cities", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<String> getCities() {
        return airportRepository.findAll().stream().map(Airport::getCity).collect(Collectors.toSet());
    }
}
