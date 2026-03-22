package ee.cgi.restaurant.service;

import ee.cgi.restaurant.dto.ReservationRequest;
import ee.cgi.restaurant.model.Reservation;
import ee.cgi.restaurant.model.RestaurantTable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ReservationService {
    private final CopyOnWriteArrayList<Reservation> reservations = new CopyOnWriteArrayList<>();

    public List<Reservation> findAll() {
        List<Reservation> list = new ArrayList<>(reservations);
        list.sort((a, b) -> a.start().compareTo(b.start()));
        return Collections.unmodifiableList(list);
    }

    public List<Reservation> findForWindow(LocalDateTime start, LocalDateTime end) {
        return findAll().stream()
                .filter(reservation -> overlaps(reservation.start(), reservation.end(), start, end))
                .toList();
    }

    public List<String> occupiedTableIds(LocalDateTime start, LocalDateTime end) {
        return findForWindow(start, end).stream()
                .flatMap(reservation -> reservation.tableIds().stream())
                .distinct()
                .toList();
    }

    public Reservation createReservation(ReservationRequest request, List<RestaurantTable> selectedTables) {
        LocalDateTime start = request.date().atTime(request.time());
        LocalDateTime end = start.plusMinutes(request.durationMinutes());
        List<String> tableIds = selectedTables.stream().map(RestaurantTable::id).toList();
        ensureAvailable(tableIds, start, end);
        Reservation reservation = new Reservation(
                UUID.randomUUID().toString(),
                tableIds,
                start,
                end,
                request.partySize(),
                request.customerName(),
                request.zone(),
                request.preferences() == null ? java.util.Set.of() : java.util.Set.copyOf(request.preferences())
        );
        reservations.add(reservation);
        return reservation;
    }

    public Reservation addSeedReservation(String customerName, List<String> tableIds, LocalDateTime start, LocalDateTime end, int partySize) {
        Reservation reservation = new Reservation(
                UUID.randomUUID().toString(),
                tableIds,
                start,
                end,
                partySize,
                customerName,
                null,
                java.util.Set.of()
        );
        reservations.add(reservation);
        return reservation;
    }

    public boolean isAvailable(List<String> tableIds, LocalDateTime start, LocalDateTime end) {
        return findForWindow(start, end).stream().noneMatch(existing ->
                existing.tableIds().stream().anyMatch(tableIds::contains));
    }

    private void ensureAvailable(List<String> tableIds, LocalDateTime start, LocalDateTime end) {
        if (!isAvailable(tableIds, start, end)) {
            throw new IllegalStateException("Selected table(s) are already reserved for the chosen time window.");
        }
    }

    private boolean overlaps(LocalDateTime aStart, LocalDateTime aEnd, LocalDateTime bStart, LocalDateTime bEnd) {
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }
}
