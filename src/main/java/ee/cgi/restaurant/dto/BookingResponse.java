package ee.cgi.restaurant.dto;

import ee.cgi.restaurant.model.MenuSuggestion;

import java.util.List;

public record BookingResponse(
        String reservationId,
        List<String> tableIds,
        String message,
        MenuSuggestion menuSuggestion
) {
    public BookingResponse {
        tableIds = List.copyOf(tableIds);
    }
}
