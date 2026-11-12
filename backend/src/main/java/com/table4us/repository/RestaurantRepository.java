package com.table4us.repository;

import com.table4us.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByActiveTrueOrderByAverageRatingDesc();
    List<Restaurant> findByCityIgnoreCaseAndActiveTrue(String city);
    List<Restaurant> findByCuisineIgnoreCaseAndActiveTrue(String cuisine);

    @Query("select r from Restaurant r where r.active = true and r.id > :afterId " +
           "and (:city is null or lower(r.city) = lower(:city)) " +
           "and (:cuisine is null or lower(r.cuisine) = lower(:cuisine)) " +
           "and (:q is null or lower(r.name) like lower(concat('%', :q, '%'))) " +
           "order by r.id")
    List<Restaurant> search(@Param("afterId") Long afterId, @Param("city") String city,
                            @Param("cuisine") String cuisine, @Param("q") String query, Pageable pageable);

    @Query("select r from Restaurant r where r.active = true and r.latitude between :south and :north " +
           "and r.longitude between :west and :east order by r.averageRating desc")
    List<Restaurant> inBounds(@Param("south") java.math.BigDecimal south, @Param("north") java.math.BigDecimal north,
                              @Param("west") java.math.BigDecimal west, @Param("east") java.math.BigDecimal east);
}
