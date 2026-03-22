package ee.cgi.restaurant.model;

import java.util.List;
import java.util.Set;

public record RestaurantTable(
        String id,
        String label,
        int seats,
        Zone zone,
        Set<TableFeature> features,
        int x,
        int y,
        int width,
        int height,
        List<String> adjacentTableIds
) {
    public RestaurantTable {
        features = Set.copyOf(features);
        adjacentTableIds = List.copyOf(adjacentTableIds);
    }

    public RestaurantTable withPosition(int newX, int newY) {
        return new RestaurantTable(id, label, seats, zone, features, newX, newY, width, height, adjacentTableIds);
    }
}
