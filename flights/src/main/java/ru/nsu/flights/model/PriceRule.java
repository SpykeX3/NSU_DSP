package ru.nsu.flights.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(schema = "bookings", name = "pricing_rules")
public class PriceRule {
    @EmbeddedId
    private PricingId id;

    @Column(name = "price")
    double price;

    public PricingId getId() {
        return id;
    }

    public void setId(PricingId id) {
        this.id = id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
