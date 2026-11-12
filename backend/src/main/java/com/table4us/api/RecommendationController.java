package com.table4us.api;
import com.table4us.repository.RestaurantRepository; import jakarta.validation.constraints.*; import org.springframework.data.domain.PageRequest; import org.springframework.validation.annotation.Validated; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @Validated @RequestMapping("/api/v1/recommendations")
public class RecommendationController {
 private final RestaurantRepository restaurants; public RecommendationController(RestaurantRepository restaurants){this.restaurants=restaurants;}
 @GetMapping public Map<String,Object> recommend(@RequestParam(required=false) String city,@RequestParam(required=false) String cuisine,@RequestParam(defaultValue="4") @Min(1) @Max(30) int people,@RequestParam(defaultValue="3000") @Min(0) int budget){
  final String selectedCity=clean(city),selectedCuisine=clean(cuisine);var entries=restaurants.search(0,selectedCity,selectedCuisine,null,PageRequest.of(0,50)).stream().map(r->{int estimate=r.getPriceLevel()*500*people;double fit=budget<=0?0:Math.max(0,1-Math.max(0,estimate-budget)/(double)budget);double score=Math.round((fit*.55+(r.getAverageRating().doubleValue()/5)*.45)*1000)/10;
   Map<String,Object> m=new LinkedHashMap<>(RestaurantController.view(r));m.put("estimatedTotal",estimate);m.put("estimatedPerPerson",estimate/people);m.put("score",score);m.put("withinBudget",estimate<=budget);m.put("explanation",estimate<=budget?"Estimated group spend fits the entered budget.":"This option is above the entered budget; compare its rating and menu.");return m;})
   .sorted(Comparator.comparingDouble((Map<String,Object> x)->((Number)x.get("score")).doubleValue()).reversed()).toList();
  return Map.of("items",entries,"notice","Estimates use an illustrative price-level formula and sample restaurant data.");
 }
 private static String clean(String v){return v==null||v.isBlank()?null:v.trim();}
}
