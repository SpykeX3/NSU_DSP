package ru.nsu.flights.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.flights.model.PriceRule;
import ru.nsu.flights.model.PricingId;

public interface PriceRepository extends JpaRepository<PriceRule, PricingId> {
}
