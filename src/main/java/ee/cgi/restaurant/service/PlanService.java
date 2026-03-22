package ee.cgi.restaurant.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import ee.cgi.restaurant.dto.BookingResponse;
import ee.cgi.restaurant.dto.PlanResponse;
import ee.cgi.restaurant.dto.ReservationRequest;
import ee.cgi.restaurant.model.MenuSuggestion;
import ee.cgi.restaurant.model.Preference;
import ee.cgi.restaurant.model.RecommendationOption;
import ee.cgi.restaurant.model.Reservation;
import ee.cgi.restaurant.model.ReservationView;
import ee.cgi.restaurant.model.RestaurantTable;
import ee.cgi.restaurant.model.SearchCriteria;
import ee.cgi.restaurant.model.TableView;
import ee.cgi.restaurant.model.Zone;

@Service
public class PlanService {
    private final TableCatalogService tableCatalogService;
    private final ReservationService reservationService;
    private final MenuSuggestionService menuSuggestionService;
    private final RecommendationEngine recommendationEngine = new RecommendationEngine();

    public PlanService(TableCatalogService tableCatalogService,
                       ReservationService reservationService,
                       MenuSuggestionService menuSuggestionService) {
        this.tableCatalogService = tableCatalogService;
        this.reservationService = reservationService;
        this.menuSuggestionService = menuSuggestionService;
    }

    public PlanResponse buildPlan(LocalDate date, LocalTime time, int partySize, int durationMinutes, Zone zone, List<Preference> preferences) {
        SearchCriteria criteria = new SearchCriteria(date, time, durationMinutes, partySize, zone, toEnumSet(preferences));
        LocalDateTime start = date.atTime(time);
        LocalDateTime end = start.plusMinutes(durationMinutes);

        Set<String> occupiedIds = Set.copyOf(reservationService.occupiedTableIds(start, end));
        List<RestaurantTable> allTables = tableCatalogService.findAll();
        List<RecommendationOption> recommendations = recommendationEngine.recommend(allTables, occupiedIds, criteria);
        List<String> recommendedIds = recommendations.isEmpty() ? List.of() : recommendations.get(0).tableIds();

        List<TableView> tableViews = allTables.stream()
                .map(table -> new TableView(
                        table.id(),
                        table.label(),
                        table.seats(),
                        table.zone(),
                        table.features().stream().toList(),
                        table.x(),
                        table.y(),
                        table.width(),
                        table.height(),
                        occupiedIds.contains(table.id()),
                        recommendedIds.contains(table.id()),
                        recommendedIds.size() > 1 && recommendedIds.contains(table.id()),
                        recommendations.isEmpty() ? 0 : recommendations.get(0).score(),
                        recommendedIds.isEmpty() || !recommendedIds.contains(table.id()) ? List.of(table.id()) : recommendedIds,
                        recommendedIds.contains(table.id()) && !recommendations.isEmpty() ? recommendations.get(0).reason() : null
                ))
                .toList();

        List<ReservationView> reservations = reservationService.findForWindow(start, end).stream()
                .map(this::toView)
                .toList();

        MenuSuggestion menuSuggestion = menuSuggestionService.suggest(partySize);

        return new PlanResponse(date, time, partySize, durationMinutes, zone, preferences == null ? List.of() : preferences, tableViews, recommendations, reservations, menuSuggestion);
    }

    public BookingResponse bookBest(ReservationRequest request) {
        List<Preference> preferences = request.preferences() == null ? List.of() : request.preferences();
        PlanResponse plan = buildPlan(request.date(), request.time(), request.partySize(), request.durationMinutes(), request.zone(), preferences);
        List<String> tableIds = request.tableIds() != null && !request.tableIds().isEmpty()
                ? request.tableIds()
                : (plan.recommendations().isEmpty() ? List.of() : plan.recommendations().get(0).tableIds());

        if (tableIds.isEmpty()) {
            throw new IllegalStateException("No suitable table could be recommended.");
        }

        List<RestaurantTable> selected = tableIds.stream()
                .map(id -> tableCatalogService.findById(id).orElseThrow(() -> new IllegalArgumentException("Unknown table: " + id)))
                .toList();

        Reservation reservation = reservationService.createReservation(request, selected);
        MenuSuggestion menuSuggestion = menuSuggestionService.suggest(request.partySize());

        return new BookingResponse(
                reservation.id(),
                reservation.tableIds(),
                "Reservation created successfully.",
                menuSuggestion
        );
    }

    private ReservationView toView(Reservation reservation) {
        return new ReservationView(
                reservation.id(),
                reservation.tableIds(),
                reservation.start(),
                reservation.end(),
                reservation.partySize(),
                reservation.customerName()
        );
    }

    private EnumSet<Preference> toEnumSet(List<Preference> preferences) {
        if (preferences == null || preferences.isEmpty()) {
            return EnumSet.noneOf(Preference.class);
        }
        return EnumSet.copyOf(preferences);
    }
}
