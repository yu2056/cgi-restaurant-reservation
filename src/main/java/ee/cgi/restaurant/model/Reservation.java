package ee.cgi.restaurant.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record Reservation(
        String id,
        List<String> tableIds,
        LocalDateTime start,
        LocalDateTime end,
        int partySize,
        String customerName,
        Zone zone,
        Set<Preference> preferences
) {
    public Reservation {
        tableIds = List.copyOf(tableIds);
        preferences = Set.copyOf(preferences);
    }
}
