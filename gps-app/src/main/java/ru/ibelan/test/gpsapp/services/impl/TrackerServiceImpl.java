package ru.ibelan.test.gpsapp.services.impl;

import lombok.NonNull;
import org.springframework.stereotype.Service;
import ru.ibelan.test.gpsapp.entities.GPS;
import ru.ibelan.test.gpsapp.entities.Point;
import ru.ibelan.test.gpsapp.entities.Schedule;
import ru.ibelan.test.gpsapp.entities.Vehicle;
import ru.ibelan.test.gpsapp.repo.GPSRepo;
import ru.ibelan.test.gpsapp.repo.ScheduleRepo;
import ru.ibelan.test.gpsapp.repo.VehicleRepo;
import ru.ibelan.test.gpsapp.services.GeoService;
import ru.ibelan.test.gpsapp.services.TrackerService;
import ru.ibelan.test.gpsapp.services.exceptions.ReceiveGpsException;
import ru.ibelan.test.gpsapp.xml.GPSPosition;

import javax.transaction.Transactional;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
public class TrackerServiceImpl implements TrackerService {
    // sufficient distance (kilometers)
    private final double SUFFICIENT_DISTANCE = 0.3;

    private final VehicleRepo vehicleRepo;
    private final GPSRepo gpsRepo;
    private final ScheduleRepo scheduleRepo;
    private final GeoService geoService;

    public TrackerServiceImpl(VehicleRepo vehicleRepo, GPSRepo gpsRepo, ScheduleRepo scheduleRepo, GeoService geoService) {
        this.vehicleRepo = vehicleRepo;
        this.gpsRepo = gpsRepo;
        this.scheduleRepo = scheduleRepo;
        this.geoService = geoService;
    }

    @Transactional
    public void receiveTrackData(GPSPosition gpsPosition) {
        String name = gpsPosition.getVehicleName();
        Vehicle vehicle = vehicleRepo.findByName(name);
        if (vehicle == null) {
            throw new ReceiveGpsException(String.format("Can't find vehicle with name \"%s\"\n", name));
        }

        Point gps = new Point(gpsPosition.getLatitude(), gpsPosition.getLongitude());

        // save gps position
        saveGps(vehicle, gpsPosition.getDateTime(), gps);

        // schedule line -> distance to vehicle
        Function<Schedule, Double> distanceToVehicle = s -> geoService.distance(s.getLocation().getCoordinates(), gps);

        // affected schedule line: reducing to one location which is closest (and closer than 300m) to vehicle location
        Schedule scheduleLine = scheduleRepo.findByVehicle(vehicle).stream()
                .min(Comparator.comparing(distanceToVehicle))
                .filter(s -> distanceToVehicle.apply(s) <= SUFFICIENT_DISTANCE)
                .orElse(null);

        // process only affected schedule lines
        if (scheduleLine != null) {
            // track data
            List<GPS> trackData = gpsRepo.findByVehicleOrderByTime(vehicle);

            Point location = scheduleLine.getLocation().getCoordinates();
            TrackDataCluster trackDataCluster = reAnalysisTrackData(trackData, location);

            if (trackDataCluster.trackDataCount > 0) {
                scheduleLine.setActualArrivalTime(trackDataCluster.from);
                scheduleLine.setActualDepartureTime(trackDataCluster.to);
            }
        }
    }

    private void saveGps(Vehicle vehicle, Date time, Point coordinates) {
        GPS gps = new GPS();
        gps.setVehicle(vehicle);
        gps.setCoordinates(coordinates);
        gps.setTime(time);
        gpsRepo.saveAndFlush(gps);
    }

    private TrackDataCluster reAnalysisTrackData(List<GPS> trackData, Point location) {
        // gps position -> distance to schedule line location
        Function<GPS, Double> distanceToLocation = g -> geoService.distance(g.getCoordinates(), location);

        TrackDataCluster preResult = new TrackDataCluster();
        TrackDataCluster result = new TrackDataCluster();

        boolean isInsideCluster = false;
        for (GPS td : trackData) {
            if (distanceToLocation.apply(td) <= SUFFICIENT_DISTANCE) {
                if (!isInsideCluster) {
                    preResult.from = td.getTime();
                }
                isInsideCluster = true;
                preResult.trackDataCount++;
                preResult.to = td.getTime();
            } else {
                // give a preference to bigger cluster
                if (isInsideCluster && preResult.compareTo(result) > 0) {
                    result.setData(preResult);
                }
                isInsideCluster = false;
            }
        }

        return result;
    }

    static class TrackDataCluster implements Comparable<TrackDataCluster> {
        private Date from = null;
        private Date to = null;
        private int trackDataCount = 0;

        @Override
        public int compareTo(@NonNull TrackDataCluster trackDataCluster) {
            return Integer.compare(this.trackDataCount, trackDataCluster.trackDataCount);
        }

        public void setData(TrackDataCluster trackDataCluster) {
            this.from = trackDataCluster.from;
            this.to = trackDataCluster.to;
            this.trackDataCount = trackDataCluster.trackDataCount;
        }
    }
}
