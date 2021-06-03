select distinct f.flight_no, f.departure_airport, f.arrival_airport, max(tf.amount) as price, tf.fare_conditions
INTO pricing_rules
FROM flights f
         JOIN ticket_flights tf ON f.flight_id = tf.flight_id
GROUP BY departure_airport, arrival_airport, tf.fare_conditions, flight_no
