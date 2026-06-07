-- Sample current_stock / minimum_stock for testing (run after products table exists).
-- Several products have current_stock < minimum_stock (shows "Low" badge in admin).

SET NAMES utf8mb4;

-- Low stock (current < minimum)
UPDATE products SET current_stock = 3,  minimum_stock = 10 WHERE id = 1;   -- Sprouted Ragi Mix
UPDATE products SET current_stock = 0,  minimum_stock = 8  WHERE id = 5;   -- Sukku Coffee
UPDATE products SET current_stock = 2,  minimum_stock = 12 WHERE id = 8;   -- Turmeric Powder
UPDATE products SET current_stock = 1,  minimum_stock = 15 WHERE id = 14;  -- Health Mix
UPDATE products SET current_stock = 4,  minimum_stock = 20 WHERE id = 19;  -- Biriyani Masala
UPDATE products SET current_stock = 0,  minimum_stock = 10 WHERE id = 26;  -- Grow Fast Mix
UPDATE products SET current_stock = 5,  minimum_stock = 25 WHERE id = 10;  -- Kumkumadhi Thailam
UPDATE products SET current_stock = 2,  minimum_stock = 6  WHERE id = 17;  -- Beef Masala

-- OK stock (current >= minimum)
UPDATE products SET current_stock = 45, minimum_stock = 10 WHERE id = 2;   -- Sprouted Ragi Powder
UPDATE products SET current_stock = 30, minimum_stock = 8  WHERE id = 3;   -- Lime Pickle
UPDATE products SET current_stock = 120, minimum_stock = 25 WHERE id = 4;  -- String Hopper Flour
UPDATE products SET current_stock = 22, minimum_stock = 6  WHERE id = 7;   -- Kodhipaal
UPDATE products SET current_stock = 15, minimum_stock = 5  WHERE id = 9;   -- Tooth Powder
UPDATE products SET current_stock = 8,  minimum_stock = 3  WHERE id = 11;  -- Hair Oil
UPDATE products SET current_stock = 14, minimum_stock = 4  WHERE id = 12;  -- Face Mask
UPDATE products SET current_stock = 40, minimum_stock = 10 WHERE id = 13;  -- Jawwarisi Papadam
UPDATE products SET current_stock = 25, minimum_stock = 8  WHERE id = 15;  -- Kollu Kanji Mix
UPDATE products SET current_stock = 12, minimum_stock = 5  WHERE id = 16;  -- ABC Malt
UPDATE products SET current_stock = 60, minimum_stock = 15 WHERE id = 18;  -- Chicken Masala
UPDATE products SET current_stock = 35, minimum_stock = 10 WHERE id = 20;  -- Curry Powder
UPDATE products SET current_stock = 28, minimum_stock = 12 WHERE id = 21;  -- Chili Powder
UPDATE products SET current_stock = 20, minimum_stock = 8  WHERE id = 22;  -- Banana Powder
UPDATE products SET current_stock = 9,  minimum_stock = 4  WHERE id = 23;  -- Manjistha Soap
UPDATE products SET current_stock = 11, minimum_stock = 5  WHERE id = 24;  -- Oil Pulling Mix
UPDATE products SET current_stock = 50, minimum_stock = 10 WHERE id = 25;  -- Pepper Powder
UPDATE products SET current_stock = 16, minimum_stock = 6  WHERE id = 27;  -- Digestive Podi
UPDATE products SET current_stock = 7,  minimum_stock = 3  WHERE id = 28;  -- Ghee
UPDATE products SET current_stock = 33, minimum_stock = 10 WHERE id = 30;  -- Fish Masala
UPDATE products SET current_stock = 6,  minimum_stock = 2  WHERE id = 31;  -- Shihaka powder

-- At minimum exactly (shows "Low" in UI because current <= minimum)
UPDATE products SET current_stock = 100, minimum_stock = 100 WHERE id = 6;   -- Aata Flour
