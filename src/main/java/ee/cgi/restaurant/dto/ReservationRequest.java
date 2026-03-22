package ee.cgi.restaurant.dto;

import ee.cgi.restaurant.model.Preference;
import ee.cgi.restaurant.model.Zone;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ReservationRequest(
        @NotBlank String customerName,
        @NotNull LocalDate date,
        @NotNull LocalTime time,
        @Min(30) @Max(720) int durationMinutes,
        @Min(1) @Max(20) int partySize,
        Zone zone,
        List<Preference> preferences,
        List<String> tableIds
) {
}
