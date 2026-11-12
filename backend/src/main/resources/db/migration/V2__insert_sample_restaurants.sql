INSERT INTO restaurant (name, description, cuisine, address, city, latitude, longitude, price_level, average_rating, booking_url, image_url)
VALUES
('Spice Garden', 'Comforting North Indian dishes for sharing.', 'North Indian', '12 MG Road', 'Bengaluru', 12.975000, 77.606000, 2, 4.5, 'https://example.com/spice-garden', NULL),
('Coastal Table', 'Fresh coastal cuisine and seafood.', 'South Indian', '8 Residency Road', 'Bengaluru', 12.966000, 77.601000, 3, 4.3, 'https://example.com/coastal-table', NULL);

INSERT INTO menu_item (restaurant_id, name, description, category, price, vegetarian, available)
SELECT r.id, sample.name, sample.description, sample.category, sample.price, sample.vegetarian, TRUE
FROM restaurant r
JOIN (VALUES
    ('Spice Garden', 'Paneer Tikka', 'Char-grilled paneer with spices', 'Starter', 320.00, TRUE),
    ('Spice Garden', 'Butter Chicken', 'Chicken in a creamy tomato sauce', 'Main', 480.00, FALSE),
    ('Spice Garden', 'Dal Makhani', 'Slow-cooked black lentils', 'Main', 350.00, TRUE),
    ('Coastal Table', 'Masala Dosa', 'Crisp dosa with potato masala', 'Main', 220.00, TRUE),
    ('Coastal Table', 'Fish Curry', 'Coastal fish curry with rice', 'Main', 520.00, FALSE)
) AS sample(restaurant_name, name, description, category, price, vegetarian)
ON r.name = sample.restaurant_name;
