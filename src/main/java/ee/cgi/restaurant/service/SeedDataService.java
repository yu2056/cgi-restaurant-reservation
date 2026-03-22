package ee.cgi.restaurant.service;

import ee.cgi.restaurant.model.RestaurantTable;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

@Component
public class SeedDataService {
    private final TableCatalogService tableCatalogService;
    private final ReservationService reservationService;
    private final Random random = new Random();

    public SeedDataService(TableCatalogService tableCatalogService, ReservationService reservationService) {
        this.tableCatalogService = tableCatalogService;
        this.reservationService = reservationService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedRandomReservations() {
        if (!reservationService.findAll().isEmpty()) {
            return;
        }
        LocalDate today = LocalDate.now();
        List<RestaurantTable> tables = tableCatalogService.findAll();
        for (RestaurantTable table : tables) {
            int bookings = random.nextInt(3); // 0..2
            for (int i = 0; i < bookings; i++) {
                createRandomReservationFor(table, today.plusDays(random.nextInt(4)));
            }
        }
    }

    private void createRandomReservationFor(RestaurantTable table, LocalDate date) {
        for (int attempt = 0; attempt < 12; attempt++) {
            int hour = 12 + random.nextInt(9); // 12:00 - 20:00
            int minute = random.nextBoolean() ? 0 : 30;
            int duration = 90 + random.nextInt(4) * 30;
            var start = date.atTime(LocalTime.of(hour, minute));
            var end = start.plusMinutes(duration);
            if (reservationService.isAvailable(List.of(table.id()), start, end)) {
                reservationService.addSeedReservation(
                        "Guest",
                        List.of(table.id()),
                        start,
                        end,
                        Math.max(1, table.seats() - random.nextInt(2))
                );
                return;
            }
        }
    }
}
