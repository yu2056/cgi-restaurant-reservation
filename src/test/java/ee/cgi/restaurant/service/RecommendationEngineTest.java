package ee.cgi.restaurant.service;

import ee.cgi.restaurant.model.Preference;
import ee.cgi.restaurant.model.RestaurantTable;
import ee.cgi.restaurant.model.SearchCriteria;
import ee.cgi.restaurant.model.TableFeature;
import ee.cgi.restaurant.model.Zone;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationEngineTest {

    private final RecommendationEngine engine = new RecommendationEngine();

    @Test
    void prefersExactFitOverLargeTable() {
        List<RestaurantTable> tables = List.of(
                new RestaurantTable("A", "A", 2, Zone.DINING_HALL, Set.of(TableFeature.WINDOW), 0, 0, 100, 70, List.of("B")),
                new RestaurantTable("B", "B", 8, Zone.DINING_HALL, Set.of(TableFeature.QUIET), 0, 0, 160, 90, List.of("A"))
        );
        SearchCriteria criteria = new SearchCriteria(LocalDate.now(), LocalTime.NOON, 120, 2, null, EnumSet.noneOf(Preference.class));

        var result = engine.recommend(tables, Set.of(), criteria);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).tableIds()).containsExactly("A");
        assertThat(result.get(0).combined()).isFalse();
    }

    @Test
    void combinesAdjacentTablesForLargeParty() {
        List<RestaurantTable> tables = List.of(
                new RestaurantTable("A", "A", 4, Zone.DINING_HALL, Set.of(TableFeature.WINDOW), 0, 0, 100, 70, List.of("B")),
                new RestaurantTable("B", "B", 4, Zone.DINING_HALL, Set.of(TableFeature.QUIET), 0, 0, 100, 70, List.of("A")),
                new RestaurantTable("C", "C", 10, Zone.DINING_HALL, Set.of(TableFeature.ACCESSIBLE), 0, 0, 180, 100, List.of())
        );
        SearchCriteria criteria = new SearchCriteria(LocalDate.now(), LocalTime.NOON, 120, 6, null, EnumSet.of(Preference.PRIVACY));

        var result = engine.recommend(tables, Set.of(), criteria);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).tableIds()).containsExactly("A", "B");
        assertThat(result.get(0).combined()).isTrue();
    }
}
