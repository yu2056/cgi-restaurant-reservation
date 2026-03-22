package ee.cgi.restaurant.model;

import java.time.LocalDateTime;
import java.util.List;

public record ReservationView(
        String id,
        List<String> tableIds,
        LocalDateTime start,
        LocalDateTime end,
        int partySize,
        String customerName
) {
    public ReservationView {
        tableIds = List.copyOf(tableIds);
    }
}
