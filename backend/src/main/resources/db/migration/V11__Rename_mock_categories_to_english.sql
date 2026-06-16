UPDATE categories
SET category_name = CASE category_id
    WHEN 'c1' THEN 'High-End Cameras'
    WHEN 'c2' THEN 'Medium Format'
    WHEN 'c3' THEN 'Mirrorless Cameras'
    WHEN 'c4' THEN 'Compact Cameras'
    WHEN 'c5' THEN 'Lenses'
    WHEN 'c6' THEN 'Accessories'
    WHEN 'a1' THEN 'Drones'
    WHEN 'a2' THEN 'Gimbals / Stabilizers'
    WHEN 'a3' THEN 'Audio Equipment'
    WHEN 'a4' THEN 'Photography Lighting'
    WHEN 'a5' THEN 'Rental Lenses'
    WHEN 'a6' THEN 'Tripods'
    ELSE category_name
END
WHERE category_id IN ('c1', 'c2', 'c3', 'c4', 'c5', 'c6', 'a1', 'a2', 'a3', 'a4', 'a5', 'a6');
