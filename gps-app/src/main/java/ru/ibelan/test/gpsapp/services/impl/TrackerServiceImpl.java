package ru.ibelan.test.gpsapp.services.impl;

import org.springframework.stereotype.Service;
import ru.ibelan.test.gpsapp.entities.Point;
import ru.ibelan.test.gpsapp.entities.Schedule;
import ru.ibelan.test.gpsapp.entities.Vehicle;
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
    private final ScheduleRepo scheduleRepo;
    private final GeoService geoService;

    public TrackerServiceImpl(VehicleRepo vehicleRepo, ScheduleRepo scheduleRepo, GeoService geoService) {
        this.vehicleRepo = vehicleRepo;
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

        // save actual movement (arrival/departure)
        Point point = new Point(gpsPosition.getLatitude(), gpsPosition.getLongitude());
        saveActualMovement(vehicle, gpsPosition.getDateTime(), point);
    }

    private void saveActualMovement(Vehicle vehicle, Date time, Point gps) {
        List<Schedule> schedules = scheduleRepo.findByVehicle(vehicle);
        Function<Schedule, Double> distanceToVehicle = s -> geoService.distance(s.getLocation().getCoordinates(), gps);

        // reduce to one location which is closest to vehicle location
        Schedule schedule = schedules.stream()
                .min(Comparator.comparing(distanceToVehicle))
                .filter(s -> distanceToVehicle.apply(s) <= SUFFICIENT_DISTANCE)
                .orElse(null);

        // set actual departure time
        schedules.stream()
                .filter(s -> s.getActualArrivalTime() != null
                        && s.getActualDepartureTime() == null
                        && s != schedule
                        && s.getActualArrivalTime().before(time)
                )
                .forEach(s -> s.setActualDepartureTime(time));

        // matching location that does not yet has an arrival time
        if (schedule != null && schedule.getActualArrivalTime() == null) {
            schedule.setActualArrivalTime(time);
        }
    }
}
