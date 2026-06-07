# Product database setup

Catalog data lives in MySQL tables (not `products.json`). Run the script once against your database:

```bash
mysql -h HOST -u USER -p DATABASE_NAME < src/main/resources/db/products.sql
```

Or paste `products.sql` into MySQL Workbench / Railway query console.

Then start the API and call **`GET /api/products`** — the response matches the former JSON shape (`{ "products": [ ... ] }`), including `currentStock` and `minimumStock`.

**Already have product tables?** Run `products-add-stock-columns.sql` instead of re-running the full seed.

**Sample stock levels (optional):** Run `products-sample-stock.sql` to set `current_stock` / `minimum_stock` — includes products where stock is below minimum for testing the “Low” badge and header notifications.

**Low-stock rule:** Notifications and the “Low” badge apply when `current_stock < minimum_stock` (not equal).

**Note:** `products.sql` includes `DROP TABLE` for product tables. Do not run it on production if you have edited product rows unless you intend to reset the catalog.
