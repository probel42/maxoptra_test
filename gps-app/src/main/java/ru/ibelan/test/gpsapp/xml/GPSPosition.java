package ru.ibelan.test.gpsapp.xml;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@JacksonXmlRootElement(localName = "GPSPosition")
public class GPSPosition {
    @JacksonXmlProperty(localName = "name")
    private String vehicleName;

    @JacksonXmlProperty(localName = "GpsTime")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date dateTime;

    @JacksonXmlProperty(localName = "Latitude")
    private double latitude;

    @JacksonXmlProperty(localName = "Longitude")
    private double longitude;
}
