package com.table4us.graphql;

import com.table4us.api.RestaurantController;
import com.table4us.entity.Reservation;
import com.table4us.entity.Restaurant;
import com.table4us.repository.MenuItemRepository;
import com.table4us.repository.RestaurantRepository;
import com.table4us.service.ReservationService;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;
import java.util.*;

@Controller
public class Table4UsGraphQLController {
    private final RestaurantRepository restaurants;
    private final MenuItemRepository menuItems;
    private final ReservationService reservations;

    public Table4UsGraphQLController(RestaurantRepository restaurants, MenuItemRepository menuItems,
            ReservationService reservations) {
        this.restaurants = restaurants;
        this.menuItems = menuItems;
        this.reservations = reservations;
    }

    @QueryMapping
    public Map<String, Object> restaurants(@Argument String city, @Argument String cuisine, @Argument Integer first,
            @Argument String after) {
        int pageSize = Math.max(1, Math.min(first == null ? 10 : first, 50));
        long afterId = decodeCursor(after);
        var rows = restaurants.search(afterId, clean(city), clean(cuisine), null,
                org.springframework.data.domain.PageRequest.of(0, pageSize + 1));
        boolean more = rows.size() > pageSize;
        if (more)
            rows = rows.subList(0, pageSize);
        List<Map<String, Object>> edges = rows.stream()
                .map(r -> Map.<String, Object>of("cursor", encodeCursor(r.getId()), "node", r)).toList();
        String end = edges.isEmpty() ? null : (String) edges.get(edges.size() - 1).get("cursor");
        return Map.of("edges", edges, "pageInfo", Map.of("hasNextPage", more, "endCursor", end == null ? "" : end));
    }

    @QueryMapping
    public Restaurant restaurant(@Argument Long id) {
        return restaurants.findById(id).filter(Restaurant::isActive).orElse(null);
    }

    @SchemaMapping(typeName = "Restaurant", field = "menuItems")
    public List<?> menuItems(Restaurant restaurant) {
        return menuItems.findByRestaurantIdAndAvailableTrueOrderByCategoryAscNameAsc(restaurant.getId());
    }

    @SchemaMapping(typeName = "Restaurant", field = "menu")
    public List<?> deprecatedMenu(Restaurant restaurant) {
        return menuItems.findByRestaurantIdAndAvailableTrueOrderByCategoryAscNameAsc(restaurant.getId());
    }

    @MutationMapping
    public Map<String, Object> createReservation(@Argument Long slotId, @Argument String guestName,
            @Argument String guestEmail, @Argument Integer partySize, @Argument String specialRequest) {
        Reservation reservation = reservations.book(slotId, guestName, guestEmail, partySize.shortValue(),
                specialRequest);
        return Map.of("id", reservation.getId(), "status", reservation.getStatus().toString(), "partySize",
                reservation.getPartySize());
    }

    private static String clean(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static String encodeCursor(Long id) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(id.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static long decodeCursor(String c) {
        if (c == null || c.isBlank())
            return 0;
        try {
            return Long
                    .parseLong(new String(Base64.getUrlDecoder().decode(c), java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            return 0;
        }
    }
}
