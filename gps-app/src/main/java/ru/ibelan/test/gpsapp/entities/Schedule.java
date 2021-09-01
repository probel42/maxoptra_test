package ru.ibelan.test.gpsapp.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "schedule")
@Data
@EqualsAndHashCode(callSuper = true)
public class Schedule extends AbstractPersistable<UUID> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "plan_arrival_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date planArrivalTime;

    @Column(name = "plan_departure_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date planDepartureTime;

    @Column(name = "actual_arrival_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date actualArrivalTime;

    @Column(name = "actual_departure_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date actualDepartureTime;
}
