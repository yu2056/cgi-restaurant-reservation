package ee.cgi.restaurant.service;

import ee.cgi.restaurant.model.MenuSuggestion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MenuSuggestionService {
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MenuSuggestion suggest(int partySize) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.themealdb.com/api/json/v1/1/random.php"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode meal = root.path("meals").path(0);
                if (!meal.isMissingNode() && !meal.isNull()) {
                    String title = meal.path("strMeal").asText("Chef's surprise");
                    String category = meal.path("strCategory").asText("Daily special");
                    String source = meal.path("strSource").asText(null);
                    String area = meal.path("strArea").asText("International");
                    return new MenuSuggestion(
                            title,
                            category,
                            source,
                            "Suggested for a group of " + partySize + " guests · " + area
                    );
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException | RuntimeException ignored) {
            // Fallback below.
        }

        List<MenuSuggestion> fallbacks = List.of(
                new MenuSuggestion("Crispy chicken bowl", "Main course", null, "Popular balanced choice"),
                new MenuSuggestion("Seasonal mushroom pasta", "Main course", null, "Comfort food for small groups"),
                new MenuSuggestion("Berry cheesecake", "Dessert", null, "Sweet finish for the table"),
                new MenuSuggestion("Soup of the day", "Starter", null, "Fast and easy recommendation")
        );
        return fallbacks.get(ThreadLocalRandom.current().nextInt(fallbacks.size()));
    }
}
