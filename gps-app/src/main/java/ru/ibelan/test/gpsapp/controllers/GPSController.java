package ru.ibelan.test.gpsapp.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.ibelan.test.gpsapp.services.TrackerService;
import ru.ibelan.test.gpsapp.xml.GPSPosition;

@RestController
public class GPSController {
    private final TrackerService trackerService;

    public GPSController(TrackerService trackerService) {
        this.trackerService = trackerService;
    }

    @PostMapping("/gps")
    public void receiveTrackData(@RequestBody GPSPosition gpsPosition) {
        trackerService.receiveTrackData(gpsPosition);
    }
}
