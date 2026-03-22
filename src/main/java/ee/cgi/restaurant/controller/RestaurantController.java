package ee.cgi.restaurant.controller;

import ee.cgi.restaurant.dto.BookingResponse;
import ee.cgi.restaurant.dto.PlanResponse;
import ee.cgi.restaurant.dto.ReservationRequest;
import ee.cgi.restaurant.model.Preference;
import ee.cgi.restaurant.model.Zone;
import ee.cgi.restaurant.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class RestaurantController {
    private final PlanService planService;

    public RestaurantController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping("/plan")
    public PlanResponse plan(
            @RequestParam LocalDate date,
            @RequestParam LocalTime time,
            @RequestParam int partySize,
            @RequestParam(defaultValue = "120") int durationMinutes,
            @RequestParam(required = false) Zone zone,
            @RequestParam(required = false) List<Preference> preferences
    ) {
        return planService.buildPlan(date, time, partySize, durationMinutes, zone, preferences);
    }

    @PostMapping("/reservations")
    public BookingResponse reserve(@Valid @RequestBody ReservationRequest request) {
        return planService.bookBest(request);
    }
}
