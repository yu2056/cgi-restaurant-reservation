package ee.cgi.restaurant.service;

import ee.cgi.restaurant.dto.ReservationRequest;
import ee.cgi.restaurant.model.Preference;
import ee.cgi.restaurant.model.Zone;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationServiceTest {

    private final ReservationService reservationService = new ReservationService();

    @Test
    void detectsOverlappingReservations() {
        var start = LocalDate.of(2026, 3, 20).atTime(18, 0);
        var end = start.plusHours(2);
        reservationService.addSeedReservation("Guest", List.of("T1"), start, end, 2);

        assertThat(reservationService.isAvailable(List.of("T1"), start.plusMinutes(30), end.plusHours(1))).isFalse();
        assertThat(reservationService.isAvailable(List.of("T2"), start.plusMinutes(30), end.plusHours(1))).isTrue();
    }
}
