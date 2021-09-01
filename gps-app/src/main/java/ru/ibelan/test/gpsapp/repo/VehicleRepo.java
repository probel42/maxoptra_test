package ru.ibelan.test.gpsapp.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ibelan.test.gpsapp.entities.Vehicle;

import java.util.UUID;

@Repository
public interface VehicleRepo extends JpaRepository<Vehicle, UUID> {
    Vehicle findByName(String name);
}
