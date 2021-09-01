package db.migration;

import com.opencsv.CSVReader;
import org.apache.commons.collections4.MapUtils;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import ru.ibelan.test.gpsapp.entities.Point;

import javax.sql.DataSource;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * load schedule from csv file
 */
public class V2__LoadSchedule extends BaseJavaMigration {
    private static final String SCHEDULE_FILE = "schedule/schedule.csv";
    private static final Integer BATCH_SIZE = 500;
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy hh:mm");

    @Override
    public void migrate(Context context) throws Exception {
        DataSource dataSource = new SingleConnectionDataSource(context.getConnection(), false);
        List<Schedule> data = new ArrayList<>();

        // load data from csv
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(getResource(SCHEDULE_FILE).getInputStream()))) {
            Iterator<String[]> iter = csvReader.iterator();
            iter.next();
            while (iter.hasNext()) {
                data.add(new Schedule(iter.next()));
            }
        }

        // save data to db
        Map<UUID, String> vehicles = saveVehicles(dataSource, data);
        Map<UUID, Point> locations = saveLocations(dataSource, data);
        saveSchedule(dataSource, data, MapUtils.invertMap(vehicles), MapUtils.invertMap(locations));
    }

    private Map<UUID, String> saveVehicles(DataSource dataSource, List<Schedule> data) {
        Iterator<String> iter = data.stream().map(d -> d.vehicle).distinct().iterator();
        return saveBatch(iter, BATCH_SIZE,
                (parameters, name) -> parameters.addValue("name", name),
                batch -> insert(dataSource, batch, "vehicle", "id", "name"));
    }

    private Map<UUID, Point> saveLocations(DataSource dataSource, List<Schedule> data) {
        Iterator<Point> iter = data.stream().map(d -> d.location).distinct().iterator();
        return saveBatch(iter, BATCH_SIZE,
                (parameters, point) -> {
                    parameters.addValue("latitude", point.getLatitude());
                    parameters.addValue("longitude", point.getLongitude());
                },
                batch -> insert(dataSource, batch, "location", "id", "latitude", "longitude"));
    }

    private void saveSchedule(DataSource dataSource, List<Schedule> data,
                              Map<String, UUID> vehicles, Map<Point, UUID> locations) {
        Iterator<Schedule> iter = data.iterator();
        saveBatch(iter, BATCH_SIZE,
                (parameters, schedule) -> {
                    parameters.addValue("vehicle_id", vehicles.get(schedule.vehicle));
                    parameters.addValue("location_id", locations.get(schedule.location));
                    parameters.addValue("plan_arrival_time", schedule.arrivalTime);
                    parameters.addValue("plan_departure_time", schedule.departuresTime);
                },
                batch -> insert(dataSource, batch, "schedule",
                        "id", "vehicle_id", "location_id", "plan_arrival_time", "plan_departure_time"));
    }

    /**
     * batch insert
     */
    private <T> Map<UUID, T> saveBatch(Iterator<T> iter, Integer batchSize,
                                       BiConsumer<MapSqlParameterSource, T> fieldsFiller,
                                       Consumer<List<MapSqlParameterSource>> inserter) {
        List<MapSqlParameterSource> batch = new ArrayList<>();
        Map<UUID, T> result = new HashMap<>();
        int i = 1;
        while (iter.hasNext()) {
            UUID id = UUID.randomUUID();
            T fields = iter.next();
            MapSqlParameterSource row = new MapSqlParameterSource();
            row.addValue("id", id);
            fieldsFiller.accept(row, fields);
            batch.add(row);
            result.put(id, fields);
            if (i % batchSize == 0 || !iter.hasNext()) {
                inserter.accept(batch);
                batch.clear();
            }
            i++;
        }
        return result;
    }

    private void insert(DataSource dataSource, List<MapSqlParameterSource> list, String tableName, String... columns) {
        new SimpleJdbcInsert(dataSource).withTableName(tableName).usingColumns(columns)
                .executeBatch(list.toArray(new SqlParameterSource[]{}));
    }

    private Resource getResource(String name) {
        return new ClassPathResource(name);
    }

    static class Schedule {
        String vehicle;
        Point location;
        Date arrivalTime;
        Date departuresTime;

        public Schedule(String[] csvRow) throws ParseException {
            this.vehicle = csvRow[0];
            this.location = new Point(Double.parseDouble(csvRow[1]), Double.parseDouble(csvRow[2]));
            this.arrivalTime = DATE_FORMATTER.parse(csvRow[3]);
            this.departuresTime = DATE_FORMATTER.parse(csvRow[4]);
        }
    }
}
