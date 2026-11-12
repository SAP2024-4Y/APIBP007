package com.table4us.repository;

import com.table4us.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Collection;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantIdAndAvailableTrueOrderByCategoryAscNameAsc(Long restaurantId);
    List<MenuItem> findByRestaurantIdInAndAvailableTrueOrderByCategoryAscNameAsc(Collection<Long> restaurantIds);
}
