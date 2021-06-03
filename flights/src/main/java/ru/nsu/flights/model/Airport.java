package ru.nsu.flights.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "airports", schema = "bookings")
public class Airport {
    @Id
    @Column(name = "airport_code")
    String code;

    @Column(name = "airport_name")
    String name;

    @Column(name = "city")
    String city;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }
}
