-- Fasa products schema + seed data (MySQL 8+)
-- Run once against your database, then use GET /api/products

SET NAMES utf8mb4;

DROP TABLE IF EXISTS product_images;
DROP TABLE IF EXISTS product_use_for;
DROP TABLE IF EXISTS product_ingredients;
DROP TABLE IF EXISTS products;

CREATE TABLE products (
  id BIGINT NOT NULL,
  name VARCHAR(200) NOT NULL,
  description TEXT,
  price INT NOT NULL,
  original_price INT NOT NULL,
  is_best_seller TINYINT(1) NOT NULL DEFAULT 0,
  weight VARCHAR(40) DEFAULT NULL,
  image VARCHAR(500) DEFAULT NULL,
  how_to_use TEXT,
  category VARCHAR(120) NOT NULL,
  current_stock INT NOT NULL DEFAULT 0,
  minimum_stock INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product_ingredients (
  product_id BIGINT NOT NULL,
  sort_order INT NOT NULL,
  ingredient VARCHAR(500) NOT NULL,
  PRIMARY KEY (product_id, sort_order),
  CONSTRAINT fk_product_ingredients_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product_use_for (
  product_id BIGINT NOT NULL,
  sort_order INT NOT NULL,
  use_for VARCHAR(500) NOT NULL,
  PRIMARY KEY (product_id, sort_order),
  CONSTRAINT fk_product_use_for_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product_images (
  product_id BIGINT NOT NULL,
  sort_order INT NOT NULL,
  image_url VARCHAR(500) NOT NULL,
  PRIMARY KEY (product_id, sort_order),
  CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== products ==========
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (1, 'Sprouted Ragi Mix', 'Nutritious sprouted ragi mix enriched with cocoa and nuts.', 370, 370, 1, '50g', '/public/images/sprouted-ragi-mix.png', 'Mix with hot milk', 'Masalas & Cooking Essentials', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (2, 'Sprouted Ragi Powder (Kurakkan)', 'Healthy sprouted ragi powder for daily nutrition.', 450, 450, 1, '100g', '/public/images/sprouted-ragi-powder.jpeg', 'Making porridge', 'Baby & Kids Products', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (3, 'Lime Pickle', 'Traditional homemade lime pickle.', 500, 500, 0, '250g', '/public/images/lime-pickle.jpeg', 'Serve with rice or rotti', 'Special Homemade Items', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (4, 'String Hopper Flour', 'Pure raw rice flour for string hoppers.', 250, 250, 1, '250g', '/public/images/string-hopper-flour.jpeg', 'Mix with hot water and prepare string hoppers', 'Masalas & Cooking Essentials', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (5, 'Sukku Coffee', 'Traditional herbal coffee mix.', 250, 250, 0, '50g', '/public/images/sukku-coffee.jpeg', 'Mix with hot water and sugar', 'Masalas & Cooking Essentials', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (6, 'Aata Flour', 'Whole wheat flour blend.', 380, 380, 0, '500g', '/public/images/aata-flour.png', 'Use in any recipe', 'Special Homemade Items', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (7, 'Kodhipaal', 'Spicy porridge mix.', 300, 300, 0, '100g', '/public/images/kodhipaal.jpeg', 'Prepare as porridge', 'Masalas & Cooking Essentials', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (8, 'Turmeric Powder', 'Pure turmeric powder.', 300, 300, 1, '50g', '/public/images/turmeric-powder.jpeg', 'Add to recipes', 'Masalas & Cooking Essentials', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (9, 'Tooth Powder', 'Natural herbal tooth powder.', 200, 200, 0, '50g', '/public/images/tooth-powder.jpeg', 'Brush teeth normally', 'Cosmetics Products', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (10, 'Kumkumadhi Thailam', 'Herbal oil for skin pigmentation and dark circles.', 2500, 2500, 0, '20ml', '/public/images/kumkumadhi-thailam.jpeg', 'Apply a few drops on face and neck, massage', 'Cosmetics Products', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (11, 'Hair Oil', 'Coconut-based hair oil for overall hair health.', 1850, 1850, 0, '200ml', '/public/images/oil.jpeg', 'Apply on hair, leave 1-2 hours, then wash', 'Cosmetics Products', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (12, 'Face Mask', 'Brightening face mask with herbal powders.', 800, 800, 0, '50g', '/public/images/face-mask.jpeg', 'Mix with rose water, milk or alovera gel, apply for 15-20 min and wash', 'Cosmetics Products', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (13, 'Jawwarisi Papadam', 'Healthy snack made from jawwarisi.', 280, 280, 0, '50g', '/public/images/javvarisi-papadam.jpeg', 'Fry in hot oil', 'Healthy Food & Traditional Mixes', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (14, 'Health Mix', 'Nutritious health mix for snacks.', 500, 500, 0, '250g', '/public/images/health-mix.jpeg', 'Add sugar, coconut, salt with warm water and make balls', 'Baby & Kids Products', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (15, 'Kollu Kanji Mix', 'Herbal mix to reduce weight and balance hormones.', 450, 450, 0, '100g', '/public/images/kollu-kanji.jpeg', 'Make as porridge to serve', 'Healthy Food & Traditional Mixes', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (16, 'ABC Malt', 'Malt mix to increase hemoglobin and brighten skin.', 800, 800, 0, '50g', '/public/images/abc-malt.jpeg', 'Mix with hot milk and serve', 'Healthy Food & Traditional Mixes', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (17, 'Beef Masala', 'Spice mix to make beef curry.', 170, 170, 0, '50g', '/public/images/beef-masala.jpeg', 'Add in beef curry', 'Masalas & Cooking Essentials', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (18, 'Chicken Masala', 'Spice mix for chicken curry.', 160, 160, 0, '50g', '/public/images/chicken-masala.jpeg', 'Add in chicken curry', 'Masalas & Cooking Essentials', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (19, 'Biriyani Masala', 'Biriyani spice mix.', 320, 320, 1, '60g', '/public/images/biriyani-masala.jpeg', 'Add in biriyani recipes', 'Masalas & Cooking Essentials', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (20, 'Curry Powder', 'Curry spice mix for multiple recipes.', 550, 550, 0, '250g', '/public/images/curry-powder.jpeg', 'Add in curry recipes', 'Masalas & Cooking Essentials', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (21, 'Chili Powder', '100% pure chili powder.', 550, 550, 0, '250g', '/public/images/chilli-powder.jpeg', 'Add in recipes', 'Masalas & Cooking Essentials', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (22, 'Banana Powder', 'Banana and nut mix to increase weight and immunity.', 600, 600, 0, '100g', '/public/images/banana-mix.jpeg', 'Add in hot milk, making pancake, muffin, and dosa', 'Baby & Kids Products', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (23, 'Manjistha Soap', 'Soap to brighten face and body.', 700, 700, 0, '50g', '/public/images/manjistha-soap.jpeg', 'Gently apply on face and body, then wash', 'Cosmetics Products', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (24, 'Oil Pulling Mix', 'Oil mix for strong and white teeth.', 650, 660, 0, '100ml', '/public/images/oil-pulling-mix.jpeg', 'Gargle in the morning', 'Cosmetics Products', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (25, 'Pepper Powder', '100% pure pepper powder.', 250, 250, 0, '50g', '/public/images/pepper-powder.png', 'Add in curry recipes', 'Masalas & Cooking Essentials', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (26, 'Grow Fast Mix', 'Porridge mix for babies after 6 months.', 800, 800, 1, '100g', '/public/images/grow-fast.jpeg', 'Prepare porridge for babies after 6 months', 'Baby & Kids Products', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (27, 'Digestive Podi', '', 380, 380, 0, '50g', '/public/images/digestive-podi.jpeg', 'Mix with hot water', 'Special Homemade Items', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (28, 'Ghee', '', 750, 750, 0, '100g', '/public/images/ghee.jpeg', 'Add in your many kind of recipes', 'Special Homemade Items', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (30, 'Fish Masala', '', 160, 160, 0, '50g', '/public/images/fish-masala.jpeg', 'Add in your fish curry recipe.', 'Masalas & Cooking Essentials', 0, 0);
INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category, current_stock, minimum_stock) VALUES (31, 'Shihaka powder', '', 800, 800, 0, '100g', '/public/images/hair-mask-powder.png', '', 'Cosmetics Products', 0, 0);

-- ========== product_ingredients ==========
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (1, 0, 'Sprouted Ragi');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (1, 1, 'Cocoa powder');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (1, 2, 'Nuts');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (2, 0, 'Sprouted Ragi');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (3, 0, 'Lemon');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (3, 1, 'Chili');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (3, 2, 'Chili flakes');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (3, 3, 'Salt');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (4, 0, '100% Raw rice');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (5, 0, 'Coffee seed');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (5, 1, 'Dried ginger');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (5, 2, 'Coriander');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (5, 3, 'Fenugreek');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (6, 0, 'Whole wheat seed');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (6, 1, 'Chickpea');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (7, 0, 'Rice');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (7, 1, 'Sukku');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (7, 2, 'Pepper');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (8, 0, '100% Turmeric');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (9, 0, 'Aralu');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (9, 1, 'Bulu');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (9, 2, 'Nelli');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (9, 3, 'Turmeric');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (9, 4, 'Salt');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (9, 5, 'Clove');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (9, 6, 'Cardamom');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (10, 0, 'Sesame oil');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (10, 1, 'Goat milk');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (10, 2, 'Dhashamoola');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (10, 3, 'Saffron');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (10, 4, 'Manjistha');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (11, 0, 'Coconut oil');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (11, 1, 'Curry leaves');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (11, 2, 'Henna');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (11, 3, 'Karisalankanni');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (11, 4, 'Fenugreek');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (11, 5, 'Hibiscus');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (11, 6, 'Vempalam pattai');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (11, 7, 'Neem');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (11, 8, 'Gotukola');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (11, 9, 'Black seed');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (11, 10, 'Alovera');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (11, 11, 'Onion');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (12, 0, 'Multani Mitti');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (12, 1, 'Rose Powder');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (12, 2, 'Kasturi Manjal');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (13, 0, 'Jawwarisi');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (13, 1, 'Salt');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (13, 2, 'Chili flake');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (13, 3, 'Cumin');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (14, 0, 'Kidney bean');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (14, 1, 'Chickpea');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (14, 2, 'Green gram');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (14, 3, 'Kurakkan');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (14, 4, 'White rice');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (14, 5, 'Red rice');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (14, 6, 'Green pea');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (14, 7, 'Kollu');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (14, 8, 'Sesame');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (14, 9, 'Ground nut');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (14, 10, 'Channa dal');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (14, 11, 'Mysoor dal');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (14, 12, 'Soya');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (14, 13, 'Wheat seed');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (14, 14, 'Corn seed');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (15, 0, 'Kollu');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (15, 1, 'Barly');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (15, 2, 'Green gram');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (15, 3, 'Fenugreek');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (15, 4, 'Pepper');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (15, 5, 'Cumin');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (15, 6, 'Sweet cumin');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (16, 0, 'Apple');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (16, 1, 'Beetroot');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (16, 2, 'Carrot');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (16, 3, 'Nuts');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (16, 4, 'Cardamom');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (17, 0, 'Coriander');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (17, 1, 'Curry leaves');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (17, 2, 'Rampe');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (17, 3, 'Cinnamon leaves');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (17, 4, 'Cumin');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (17, 5, 'Sweet cumin');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (17, 6, 'Turmeric');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (17, 7, 'Chili');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (17, 8, 'Garlic');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (17, 9, 'Cinnamon');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (17, 10, 'Clove');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (17, 11, 'Cardamom');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (17, 12, 'Pepper');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (18, 0, 'Coriander');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (18, 1, 'Curry leaves');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (18, 2, 'Rampe');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (18, 3, 'Cinnamon leaves');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (18, 4, 'Cumin');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (18, 5, 'Sweet cumin');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (18, 6, 'Turmeric');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (18, 7, 'Chili');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (18, 8, 'Cinnamon');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (18, 9, 'Clove');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (18, 10, 'Cardamom');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (18, 11, 'Chickpea');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (18, 12, 'Pepper');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (19, 0, 'Coriander');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (19, 1, 'Sweet cumin');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (19, 2, 'Cumin');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (19, 3, 'Chili');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (19, 4, 'Cardamom');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (19, 5, 'Cinnamon');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (19, 6, 'Clove');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (19, 7, 'Pepper');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (19, 8, 'Nutmeg');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (19, 9, 'Biriyani leaves');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (19, 10, 'Cinnamon leaves');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (20, 0, 'Coriander');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (20, 1, 'Cumin');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (20, 2, 'Sweet cumin');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (20, 3, 'Chickpea');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (20, 4, 'Cardamom');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (20, 5, 'Clove');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (20, 6, 'Cinnamon');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (20, 7, 'Curry leaves');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (20, 8, 'Rampe');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (20, 9, 'Cinnamon leaves');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (21, 0, '100% chili');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (22, 0, 'Koli kuttu');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (22, 1, 'Banana');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (22, 2, 'Nuts');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (23, 0, 'Coconut oil');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (23, 1, 'Manjistha oil');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (23, 2, 'Castor oil');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (23, 3, 'Butter');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (23, 4, 'Caustic soda');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (24, 0, 'Sesame oil');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (24, 1, 'Clove');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (25, 0, '100% pepper seed');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (26, 0, 'Kuruluthuda');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (26, 1, 'Dal');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (26, 2, 'Ragi');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (26, 3, 'Sweet potato');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (26, 4, 'Carrot');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (26, 5, 'Moringa');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (26, 6, 'Banana');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (27, 0, 'Black Seeds');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (27, 1, 'Fenugreek');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (27, 2, 'Omam');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (28, 0, '100% Pure Butter');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (28, 1, 'Morning Leaves');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (30, 0, 'Coriander');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (30, 1, 'Sweet Cumin');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (30, 2, 'Cumin');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (30, 3, 'Chilli');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (30, 4, 'Turmeric');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (30, 5, 'Rice');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (30, 6, 'Pepper');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (30, 7, 'Curry Leaves');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (30, 8, 'Garcinia');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (31, 0, 'Shihaka');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (31, 1, 'Soap Nut');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (31, 2, 'Fenugreek');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (31, 3, 'Clove');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (31, 4, 'Green Gram');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (31, 5, 'Vetiver');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (31, 6, 'Henna');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (31, 7, 'Hibiscus');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (31, 8, 'Curry Leaves');
INSERT INTO product_ingredients (product_id, sort_order, ingredient) VALUES (31, 9, 'Nelli');

-- ========== product_use_for ==========
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (1, 0, 'Weight gain');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (1, 1, 'Increase immunity');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (1, 2, 'Increase calcium');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (2, 0, 'Increase immunity');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (3, 0, 'Eating with rice');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (4, 0, 'Making string hoppers');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (5, 0, 'Hot coffee');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (6, 0, 'Rotti');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (6, 1, 'Chapatti');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (6, 2, 'Cakes');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (7, 0, 'Spicy porridge');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (8, 0, 'Cooking');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (9, 0, 'Brushing teeth');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (10, 0, 'Curing dark circles');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (10, 1, 'Pigmentation');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (10, 2, 'Uneven tone');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (11, 0, 'Cure hair problems');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (12, 0, 'Brightening face');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (13, 0, 'Making snack');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (14, 0, 'Healthy snacks');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (15, 0, 'Lose weight');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (15, 1, 'Reduce diabetic');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (15, 2, 'Hormone balance');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (16, 0, 'Increase Hb level');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (16, 1, 'Brighten skin');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (17, 0, 'Beef curry');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (18, 0, 'Chicken curry');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (19, 0, 'Biriyani recipes');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (20, 0, 'Cooking recipes');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (21, 0, 'Cooking recipes');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (22, 0, 'Increase weight');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (22, 1, 'Increase immunity');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (23, 0, 'Brighten face and body');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (24, 0, 'Strong and white teeth');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (25, 0, 'Cooking recipes');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (26, 0, 'Porridge for babies');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (27, 0, 'To cure digestive problems.');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (28, 0, 'To make sweets and dishes.');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (30, 0, 'To make fish curry.');
INSERT INTO product_use_for (product_id, sort_order, use_for) VALUES (31, 0, 'As a cosmetic product.');

-- ========== product_images ==========
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (1, 0, '/public/images/sprouted-ragi-mix.png');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (2, 0, '/public/images/sprouted-ragi-powder.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (3, 0, '/public/images/lime-pickle.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (4, 0, '/public/images/string-hopper-flour.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (5, 0, '/public/images/sukku-coffee.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (6, 0, '/public/images/aata-flour.png');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (7, 0, '/public/images/kodhipaal.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (8, 0, '/public/images/turmeric-powder.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (9, 0, '/public/images/tooth-powder.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (10, 0, '/public/images/kumkumadhi-thailam.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (11, 0, '/public/images/oil.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (12, 0, '/public/images/face-mask.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (13, 0, '/public/images/javvarisi-papadam.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (14, 0, '/public/images/health-mix.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (15, 0, '/public/images/kollu-kanji.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (16, 0, '/public/images/abc-malt.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (17, 0, '/public/images/beef-masala.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (18, 0, '/public/images/chicken-masala.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (19, 0, '/public/images/biriyani-masala.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (20, 0, '/public/images/curry-powder.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (21, 0, '/public/images/chilli-powder.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (22, 0, '/public/images/banana-mix.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (23, 0, '/public/images/manjistha-soap.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (24, 0, '/public/images/oil-pulling-mix.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (25, 0, '/public/images/pepper-powder.png');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (26, 0, '/public/images/grow-fast.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (27, 0, '/public/images/digestive-podi.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (28, 0, '/public/images/ghee.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (30, 0, '/public/images/fish-masala.jpeg');
INSERT INTO product_images (product_id, sort_order, image_url) VALUES (31, 0, '/public/images/hair-mask-powder.png');

