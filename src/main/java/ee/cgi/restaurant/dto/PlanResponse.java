package ee.cgi.restaurant.dto;

import ee.cgi.restaurant.model.MenuSuggestion;
import ee.cgi.restaurant.model.Preference;
import ee.cgi.restaurant.model.ReservationView;
import ee.cgi.restaurant.model.TableView;
import ee.cgi.restaurant.model.Zone;
import ee.cgi.restaurant.model.RecommendationOption;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record PlanResponse(
        LocalDate date,
        LocalTime time,
        int partySize,
        int durationMinutes,
        Zone zone,
        List<Preference> preferences,
        List<TableView> tables,
        List<RecommendationOption> recommendations,
        List<ReservationView> reservations,
        MenuSuggestion menuSuggestion
) {
}
