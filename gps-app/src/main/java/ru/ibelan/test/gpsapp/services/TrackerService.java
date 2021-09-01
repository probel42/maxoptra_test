package ru.ibelan.test.gpsapp.services;

import ru.ibelan.test.gpsapp.xml.GPSPosition;

public interface TrackerService {
    void receiveTrackData(GPSPosition gpsPosition);
}
