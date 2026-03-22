package ee.cgi.restaurant.model;

import java.util.List;

public record RecommendationOption(
        List<String> tableIds,
        int seats,
        int score,
        String reason,
        boolean combined
) {
    public RecommendationOption {
        tableIds = List.copyOf(tableIds);
    }
}
