package ru.ibelan.test.gpsapp.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "vehicle")
@Data
@EqualsAndHashCode(callSuper = true)
public class Vehicle extends AbstractPersistable<UUID> {
    /**
     * Vehicle number
     */
    @Column(name = "name", nullable = false)
    private String name;
}
