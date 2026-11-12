package com.table4us.api;

import com.table4us.entity.MenuItem;
import com.table4us.entity.Restaurant;
import com.table4us.repository.MenuItemRepository;
import com.table4us.repository.RestaurantRepository;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@Validated
@RequestMapping("/api/v1/restaurants")
public class RestaurantController {
    private final RestaurantRepository restaurants;
    private final MenuItemRepository menuItems;
    public RestaurantController(RestaurantRepository restaurants, MenuItemRepository menuItems) {
        this.restaurants = restaurants; this.menuItems = menuItems;
    }

    @GetMapping
    public Map<String,Object> list(@RequestParam(required=false) String city,
            @RequestParam(required=false) String cuisine, @RequestParam(required=false) String q,
            @RequestParam(required=false) String after, @RequestParam(defaultValue="10") @Min(1) @Max(50) int limit) {
        long afterId = decode(after);
        var rows = restaurants.search(afterId, blank(city), blank(cuisine), blank(q), PageRequest.of(0, limit + 1));
        boolean more = rows.size() > limit;
        if (more) rows = rows.subList(0, limit);
        String next = more && !rows.isEmpty() ? Base64.getUrlEncoder().withoutPadding().encodeToString(
                Long.toString(rows.get(rows.size()-1).getId()).getBytes(StandardCharsets.UTF_8)) : null;
        return Map.of("items", rows.stream().map(RestaurantController::view).toList(), "nextCursor", next == null ? "" : next, "hasMore", more);
    }

    @GetMapping("/{id}") public Map<String,Object> get(@PathVariable long id) {
        return restaurants.findById(id).filter(Restaurant::isActive).map(RestaurantController::view)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RESTAURANT_NOT_FOUND"));
    }
    @GetMapping("/{id}/menu") public List<Map<String,Object>> menu(@PathVariable long id) {
        if (!restaurants.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "RESTAURANT_NOT_FOUND");
        return menuItems.findByRestaurantIdAndAvailableTrueOrderByCategoryAscNameAsc(id).stream().map(RestaurantController::menuView).toList();
    }
    @GetMapping("/map") public List<Map<String,Object>> bounds(@RequestParam @DecimalMin("-90") BigDecimal south,
        @RequestParam @DecimalMax("90") BigDecimal north, @RequestParam @DecimalMin("-180") BigDecimal west,
        @RequestParam @DecimalMax("180") BigDecimal east) {
        if (south.compareTo(north)>0 || west.compareTo(east)>0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"INVALID_BOUNDS");
        return restaurants.inBounds(south,north,west,east).stream().map(RestaurantController::view).toList();
    }
    static Map<String,Object> view(Restaurant r) {
        Map<String,Object> m=new LinkedHashMap<>(); m.put("id",r.getId());m.put("name",r.getName());m.put("description",r.getDescription());
        m.put("cuisine",r.getCuisine());m.put("address",r.getAddress());m.put("city",r.getCity());m.put("latitude",r.getLatitude());
        m.put("longitude",r.getLongitude());m.put("priceLevel",r.getPriceLevel());m.put("averageRating",r.getAverageRating());
        m.put("bookingUrl",r.getBookingUrl());m.put("sampleData",true);return m;
    }
    private static Map<String,Object> menuView(MenuItem i) { return Map.of("id",i.getId(),"name",i.getName(),"description",Objects.toString(i.getDescription(),""),"category",Objects.toString(i.getCategory(),""),"price",i.getPrice(),"vegetarian",i.isVegetarian()); }
    private static String blank(String s){return s==null||s.isBlank()?null:s.trim();}
    private static long decode(String c){if(c==null||c.isBlank())return 0;try{return Long.parseLong(new String(Base64.getUrlDecoder().decode(c),StandardCharsets.UTF_8));}catch(Exception e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"INVALID_CURSOR");}}
}
