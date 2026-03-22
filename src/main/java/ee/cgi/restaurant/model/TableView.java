package ee.cgi.restaurant.model;

import java.util.List;

public record TableView(
        String id,
        String label,
        int seats,
        Zone zone,
        List<TableFeature> features,
        int x,
        int y,
        int width,
        int height,
        boolean occupied,
        boolean recommended,
        boolean combined,
        int score,
        List<String> tableIds,
        String reason
) {
    public TableView {
        features = List.copyOf(features);
        tableIds = List.copyOf(tableIds);
    }
}
