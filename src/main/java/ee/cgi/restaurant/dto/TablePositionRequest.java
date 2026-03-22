package ee.cgi.restaurant.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TablePositionRequest(
        @NotNull @Min(0) Integer x,
        @NotNull @Min(0) Integer y
) {
}
