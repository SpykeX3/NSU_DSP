package ru.nsu.flights.model;

import java.util.List;

public class FullRoute {
    private double price;
    private List<Route> flights;

    public FullRoute() {
    }

    public FullRoute(List<Route> flights) {
        this.flights = flights;
        this.price = flights.stream().map(Route::getPrice).reduce(Double::sum).orElse(0.);

    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<Route> getFlights() {
        return flights;
    }

    public void setFlights(List<Route> flights) {
        this.flights = flights;
        this.price = flights.stream().map(Route::getPrice).reduce(Double::sum).orElse(0.);
    }
}
