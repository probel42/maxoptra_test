package ru.ibelan.test.gpsapp.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.ibelan.test.gpsapp.entities.Point;

@SpringBootTest
public class GeoServiceTest {
    // data from time-in.ru
    private final static Point MOSCOW = new Point(55.7522, 37.6156);
    private final static Point LONDON = new Point(51.5085, -0.12574);

    private final static double MOSCOW_LONDON_DISTANCE = 2500; // 2500km
    private final static double PERMISSIBLE_VARIATION = 1; // 1km

    @Autowired
    private GeoService geoService;

    @Test
    void distanceTest() {
        double distance = geoService.distance(MOSCOW, LONDON);
        Assertions.assertTrue(Math.abs(MOSCOW_LONDON_DISTANCE - distance) < PERMISSIBLE_VARIATION);
    }
}
