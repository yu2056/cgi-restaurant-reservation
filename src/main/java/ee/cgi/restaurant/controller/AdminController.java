package ee.cgi.restaurant.controller;

import ee.cgi.restaurant.dto.TablePositionRequest;
import ee.cgi.restaurant.model.RestaurantTable;
import ee.cgi.restaurant.service.TableCatalogService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final TableCatalogService tableCatalogService;

    public AdminController(TableCatalogService tableCatalogService) {
        this.tableCatalogService = tableCatalogService;
    }

    @GetMapping("/tables")
    public List<RestaurantTable> tables() {
        return tableCatalogService.findAll();
    }

    @PutMapping("/tables/{id}/position")
    public RestaurantTable updatePosition(@PathVariable String id, @Valid @RequestBody TablePositionRequest request) {
        return tableCatalogService.updatePosition(id, request.x(), request.y());
    }
}
