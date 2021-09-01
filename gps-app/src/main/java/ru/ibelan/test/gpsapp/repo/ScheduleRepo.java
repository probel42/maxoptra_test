package ru.ibelan.test.gpsapp.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ibelan.test.gpsapp.entities.Schedule;
import ru.ibelan.test.gpsapp.entities.Vehicle;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduleRepo extends JpaRepository<Schedule, UUID>  {
    List<Schedule> findByVehicle(Vehicle vehicle);
}
