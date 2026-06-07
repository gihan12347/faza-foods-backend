-- Run on an existing database that already has the products table (no DROP).
-- Safe to run once; skip if columns already exist.

ALTER TABLE products
  ADD COLUMN current_stock INT NOT NULL DEFAULT 0,
  ADD COLUMN minimum_stock INT NOT NULL DEFAULT 0;
