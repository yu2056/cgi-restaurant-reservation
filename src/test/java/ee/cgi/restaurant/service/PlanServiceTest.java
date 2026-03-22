package ee.cgi.restaurant.service;

import ee.cgi.restaurant.dto.ReservationRequest;
import ee.cgi.restaurant.model.Preference;
import ee.cgi.restaurant.model.Zone;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlanServiceTest {

    @Test
    void booksBestTableWhenTableIdsAreNotProvided() {
        TableCatalogService catalog = new TableCatalogService();
        ReservationService reservations = new ReservationService();
        MenuSuggestionService menu = new MenuSuggestionService();
        PlanService service = new PlanService(catalog, reservations, menu);

        var response = service.bookBest(new ReservationRequest(
                "Marta",
                LocalDate.of(2026, 3, 20),
                LocalTime.of(19, 0),
                120,
                2,
                null,
                List.of(Preference.WINDOW),
                null
        ));

        assertThat(response.tableIds()).isNotEmpty();
        assertThat(response.message()).contains("success");
    }
}
