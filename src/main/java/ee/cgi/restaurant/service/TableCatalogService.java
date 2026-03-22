package ee.cgi.restaurant.service;

import ee.cgi.restaurant.model.RestaurantTable;
import ee.cgi.restaurant.model.TableFeature;
import ee.cgi.restaurant.model.Zone;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class TableCatalogService {
    private final AtomicReference<List<RestaurantTable>> tables = new AtomicReference<>(createDefaultTables());

    public List<RestaurantTable> findAll() {
        return List.copyOf(tables.get());
    }

    public Optional<RestaurantTable> findById(String id) {
        return tables.get().stream().filter(table -> table.id().equals(id)).findFirst();
    }

    public RestaurantTable updatePosition(String id, int x, int y) {
        List<RestaurantTable> current = tables.get();
        List<RestaurantTable> updated = new ArrayList<>(current.size());
        RestaurantTable found = null;
        for (RestaurantTable table : current) {
            if (table.id().equals(id)) {
                found = table.withPosition(x, y);
                updated.add(found);
            } else {
                updated.add(table);
            }
        }
        if (found == null) {
            throw new IllegalArgumentException("Table not found: " + id);
        }
        tables.set(List.copyOf(updated));
        return found;
    }

    private static List<RestaurantTable> createDefaultTables() {
        return List.of(
                new RestaurantTable("T1", "T1", 2, Zone.DINING_HALL, EnumSet.of(TableFeature.WINDOW, TableFeature.CORNER), 70, 90, 90, 70, List.of("T2", "T3")),
                new RestaurantTable("T2", "T2", 4, Zone.DINING_HALL, EnumSet.of(TableFeature.WINDOW), 190, 90, 110, 75, List.of("T1", "T4")),
                new RestaurantTable("T3", "T3", 2, Zone.DINING_HALL, EnumSet.of(TableFeature.QUIET), 70, 190, 90, 70, List.of("T1", "T5")),
                new RestaurantTable("T4", "T4", 4, Zone.DINING_HALL, EnumSet.of(TableFeature.ACCESSIBLE), 190, 190, 110, 75, List.of("T2", "T6")),
                new RestaurantTable("T5", "T5", 2, Zone.DINING_HALL, EnumSet.of(TableFeature.NEAR_PLAY_AREA), 70, 290, 90, 70, List.of("T3", "T7")),
                new RestaurantTable("T6", "T6", 6, Zone.DINING_HALL, EnumSet.of(TableFeature.QUIET, TableFeature.ACCESSIBLE), 320, 120, 135, 85, List.of("T4", "T8")),
                new RestaurantTable("T7", "T7", 4, Zone.TERRACE, EnumSet.of(TableFeature.TERRACE_VIEW, TableFeature.WINDOW), 590, 80, 120, 75, List.of("T8")),
                new RestaurantTable("T8", "T8", 4, Zone.TERRACE, EnumSet.of(TableFeature.TERRACE_VIEW), 730, 80, 120, 75, List.of("T7", "T9")),
                new RestaurantTable("T9", "T9", 8, Zone.PRIVATE_ROOM, EnumSet.of(TableFeature.QUIET, TableFeature.CORNER), 560, 260, 150, 95, List.of("T10")),
                new RestaurantTable("T10", "T10", 8, Zone.PRIVATE_ROOM, EnumSet.of(TableFeature.ACCESSIBLE, TableFeature.QUIET), 740, 260, 150, 95, List.of("T9")),
                new RestaurantTable("T11", "T11", 2, Zone.DINING_HALL, EnumSet.of(TableFeature.NEAR_PLAY_AREA), 320, 330, 90, 70, List.of("T4", "T6")),
                new RestaurantTable("T12", "T12", 4, Zone.DINING_HALL, EnumSet.of(TableFeature.WINDOW, TableFeature.CORNER), 460, 120, 110, 75, List.of("T6", "T7"))
        );
    }
}
