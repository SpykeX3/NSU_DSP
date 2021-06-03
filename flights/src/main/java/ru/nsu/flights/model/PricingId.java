package ru.nsu.flights.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class PricingId implements Serializable {
    @Column(name = "flight_no")
    private String flightNo;

    @Column(name = "fare_conditions")
    private String fareConditions;

    public PricingId() {
    }

    public PricingId(String flightNo, String fareConditions) {
        this.flightNo = flightNo;
        this.fareConditions = fareConditions;
    }

    public String getFlightNo() {
        return flightNo;
    }

    public void setFlightNo(String flightNo) {
        this.flightNo = flightNo;
    }

    public String getFareConditions() {
        return fareConditions;
    }

    public void setFareConditions(String fareConditions) {
        this.fareConditions = fareConditions;
    }
}
