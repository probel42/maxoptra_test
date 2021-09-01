package ru.ibelan.test.gpsapp.services.impl;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.impl.PointImpl;
import org.springframework.stereotype.Service;
import ru.ibelan.test.gpsapp.entities.Point;
import ru.ibelan.test.gpsapp.services.GeoService;

/**
 * Spatial4j wrapper
 */
@Service
public class GeoServiceImpl implements GeoService {
    private static final SpatialContext spatialContext = SpatialContext.GEO;

    @Override
    public double distance(Point a, Point b) {
        double distance = spatialContext.getDistCalc().distance(getPoint(a), getPoint(b));
        return distance * DistanceUtils.DEG_TO_KM;
    }

    private com.spatial4j.core.shape.Point getPoint(Point point) {
        return new PointImpl(point.getLongitude(), point.getLatitude(), spatialContext);
    }
}
