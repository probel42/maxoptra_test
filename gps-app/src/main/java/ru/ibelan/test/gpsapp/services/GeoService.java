package ru.ibelan.test.gpsapp.services;

import ru.ibelan.test.gpsapp.entities.Point;

public interface GeoService {
    double distance(Point a, Point b);
}
