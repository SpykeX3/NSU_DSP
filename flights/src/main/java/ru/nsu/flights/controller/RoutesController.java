package ru.nsu.flights.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.flights.model.Airport;
import ru.nsu.flights.model.Flight;
import ru.nsu.flights.model.FullRoute;
import ru.nsu.flights.model.PricingId;
import ru.nsu.flights.model.Route;
import ru.nsu.flights.model.TicketClass;
import ru.nsu.flights.repository.AirportRepository;
import ru.nsu.flights.repository.FlightsRepository;
import ru.nsu.flights.repository.PriceRepository;
import ru.nsu.flights.repository.RoutesRepository;

@RestController
@RequestMapping(value = "routes/")
public class RoutesController {

    private final RoutesRepository routesRepository;
    private final FlightsRepository flightsRepository;
    private final AirportRepository airportRepository;
    private final PriceRepository priceRepository;

    @Autowired
    public RoutesController(RoutesRepository routesRepository,
                            FlightsRepository flightsRepository,
                            AirportRepository airportRepository,
                            PriceRepository priceRepository) {
        this.routesRepository = routesRepository;
        this.flightsRepository = flightsRepository;
        this.airportRepository = airportRepository;
        this.priceRepository = priceRepository;
    }

    @GetMapping(value = "arrival", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Route> getRoutesForArrivalAirport(@RequestParam(name = "airportCode") String airport) {
        return routesRepository.findRouteDTOSByArrivalAirport(airport).stream()
                .peek(route -> {
                    Flight flight = flightsRepository.findFirstByFlightNo(route.getFlightNo());
                    route.setTimeOfArrival(flight.getArrivalTime().substring(11));
                    route.setTimeOfDeparture(flight.getArrivalTime().substring(11));
                })
                .collect(Collectors.toList());
    }

    @GetMapping(value = "departure", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Route> getRoutesForDepartureAirport(@RequestParam(name = "airportCode") String airport) {
        return routesRepository.findRouteDTOSByDepartureAirport(airport).stream()
                .peek(route -> {
                    Flight flight = flightsRepository.findFirstByFlightNo(route.getFlightNo());
                    route.setTimeOfArrival(flight.getArrivalTime().substring(11));
                    route.setTimeOfDeparture(flight.getDepartureTime().substring(11));
                })
                .collect(Collectors.toList());
    }

    @GetMapping(value = "find", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FullRoute> getRoutesForDepartureAirport(@RequestParam(name = "fromCity", required = false) String fromCity,
                                                        @RequestParam(name = "fromAirport", required = false) String fromAirport,
                                                        @RequestParam(name = "toCity", required = false) String toCity,
                                                        @RequestParam(name = "toAirport", required = false) String toAirport,
                                                        @RequestParam(name = "date") String date,
                                                        @RequestParam(name = "indirect", required = false) boolean indirect,
                                                        @RequestParam(name = "class") TicketClass ticketClass) {
        if (fromAirport == null && fromCity == null) {
            throw new RuntimeException("No source point");
        }
        if (toAirport == null && toCity == null) {
            throw new RuntimeException("No destination point");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
        LocalDate localDate = LocalDate.parse(date, formatter);
        String dow = String.valueOf(localDate.getDayOfWeek().getValue());
        List<FullRoute> candidates = new ArrayList<>();
        List<Airport> fromList = fromAirport == null ? airportRepository.findAirportsByCity(fromCity) :
                List.of(airportRepository.getOne(fromAirport));
        List<Airport> toList = toAirport == null ? airportRepository.findAirportsByCity(toCity) :
                List.of(airportRepository.getOne(toAirport));
        for (Airport from : fromList) {
            for (Airport to : toList) {
                candidates.addAll(routesRepository.findRouteDTOSByDepartureAirportAndArrivalAirport(from.getCode(),
                        to.getCode()).stream()
                        .filter(route -> route.getDaysOfWeek().contains(dow)
                                && priceRepository.existsById(new PricingId(route.getFlightNo(),
                                ticketClass.toString())))
                        .peek(r -> r.setPrice(
                                priceRepository.findById(new PricingId(r.getFlightNo(), ticketClass.toString()))
                                        .orElseThrow(RuntimeException::new).getPrice()))
                        .map(r -> new FullRoute(List.of(r)))
                        .collect(Collectors.toList()));
            }
        }
        if (indirect) {
            Set<Route> firstCandidates = new HashSet<>();
            Set<Route> secondCandidates = new HashSet<>();
            for (Airport from : fromList) {
                firstCandidates.addAll(routesRepository.findRouteDTOSByDepartureAirport(from.getCode()).stream()
                        .filter(route -> route.getDaysOfWeek().contains(dow)
                                && priceRepository.existsById(new PricingId(route.getFlightNo(),
                                ticketClass.toString())))
                        .collect(Collectors.toList()));
            }
            for (Airport to : toList) {
                secondCandidates.addAll(
                        routesRepository.findRouteDTOSByArrivalAirport(to.getCode()).stream()
                                .filter(route -> route.getDaysOfWeek().contains(dow)
                                        && priceRepository.existsById(new PricingId(route.getFlightNo(),
                                        ticketClass.toString())))
                                .collect(Collectors.toList()));
            }
            fillRoutes(ticketClass, firstCandidates);
            fillRoutes(ticketClass, secondCandidates);
            for (var first : firstCandidates) {
                for (var second : secondCandidates) {
                    if (!first.getArrivalAirport().equals(second.getDepartureAirport())) {
                        continue;
                    }
                    if (first.getTimeOfArrival().compareTo(second.getTimeOfArrival()) > 0) {
                        continue;
                    }
                    candidates.add(new FullRoute(List.of(first, second)));
                }
            }
        }
        return candidates;
    }

    private void fillRoutes(TicketClass ticketClass, Set<Route> secondCandidates) {
        secondCandidates.forEach(route -> {
            Flight flight = flightsRepository.findFirstByFlightNo(route.getFlightNo());
            route.setTimeOfArrival(flight.getArrivalTime().substring(11));
            route.setTimeOfDeparture(flight.getDepartureTime().substring(11));
            route.setPrice(priceRepository
                    .findById(new PricingId(route.getFlightNo(), ticketClass.toString()))
                    .orElseThrow(RuntimeException::new).getPrice());
        });
    }
}
