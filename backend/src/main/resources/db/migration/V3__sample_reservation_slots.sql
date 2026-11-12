-- Demo-only availability, generated relative to migration date.
INSERT INTO reservation_slot (restaurant_id, starts_at, duration_minutes, capacity_people)
SELECT r.id, (CURRENT_DATE + day_offset + time_value)::timestamp AT TIME ZONE 'Asia/Kolkata', 120, 24
FROM restaurant r
CROSS JOIN generate_series(1, 7) AS day_offset
CROSS JOIN (VALUES (TIME '18:00'), (TIME '19:00'), (TIME '20:00'), (TIME '21:00')) AS times(time_value)
WHERE r.name IN ('Spice Garden', 'Coastal Table');
