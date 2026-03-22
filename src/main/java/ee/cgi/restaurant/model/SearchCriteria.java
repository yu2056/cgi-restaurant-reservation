package ee.cgi.restaurant.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;

public record SearchCriteria(
        LocalDate date,
        LocalTime time,
        int durationMinutes,
        int partySize,
        Zone zone,
        EnumSet<Preference> preferences
) {
}
