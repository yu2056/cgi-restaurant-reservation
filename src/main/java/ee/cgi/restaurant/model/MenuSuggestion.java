package ee.cgi.restaurant.model;

public record MenuSuggestion(
        String title,
        String category,
        String sourceUrl,
        String note
) {
}
