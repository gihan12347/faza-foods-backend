# -*- coding: utf-8 -*-
"""Generate src/main/resources/db/products.sql from data/products.json."""
import json
import pathlib

ROOT = pathlib.Path(__file__).resolve().parents[1]
JSON_PATH = ROOT / "src/main/resources/data/products.json"
SQL_PATH = ROOT / "src/main/resources/db/products.sql"


def esc(s):
    if s is None:
        return ""
    return str(s).replace("\\", "\\\\").replace("'", "''")


def main():
    data = json.loads(JSON_PATH.read_text(encoding="utf-8"))
    lines = [
        "-- Fasa products schema + seed data (MySQL 8+)",
        "-- Run once against your database, then use GET /api/products",
        "",
        "SET NAMES utf8mb4;",
        "",
        "DROP TABLE IF EXISTS product_images;",
        "DROP TABLE IF EXISTS product_use_for;",
        "DROP TABLE IF EXISTS product_ingredients;",
        "DROP TABLE IF EXISTS products;",
        "",
        "CREATE TABLE products (",
        "  id BIGINT NOT NULL,",
        "  name VARCHAR(200) NOT NULL,",
        "  description TEXT,",
        "  price INT NOT NULL,",
        "  original_price INT NOT NULL,",
        "  is_best_seller TINYINT(1) NOT NULL DEFAULT 0,",
        "  weight VARCHAR(40) DEFAULT NULL,",
        "  image VARCHAR(500) DEFAULT NULL,",
        "  how_to_use TEXT,",
        "  category VARCHAR(120) NOT NULL,",
        "  PRIMARY KEY (id)",
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;",
        "",
    ]
    for table, col in [
        ("product_ingredients", "ingredient"),
        ("product_use_for", "use_for"),
        ("product_images", "image_url"),
    ]:
        lines.extend([
            f"CREATE TABLE {table} (",
            "  product_id BIGINT NOT NULL,",
            "  sort_order INT NOT NULL,",
            f"  {col} VARCHAR(500) NOT NULL,",
            "  PRIMARY KEY (product_id, sort_order),",
            f"  CONSTRAINT fk_{table}_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE",
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;",
            "",
        ])

    lines.append("-- ========== products ==========")
    for pr in data["products"]:
        bs = 1 if pr.get("isBestSeller") else 0
        lines.append(
            "INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category) VALUES ("
        )
        lines.append(
            "  {id}, '{name}', '{desc}', {price}, {orig}, {bs}, '{weight}', '{image}', '{how}', '{cat}');".format(
                id=pr["id"],
                name=esc(pr["name"]),
                desc=esc(pr.get("description", "")),
                price=pr["price"],
                orig=pr["originalPrice"],
                bs=bs,
                weight=esc(pr.get("weight", "")),
                image=esc(pr.get("image", "")),
                how=esc(pr.get("howToUse", "")),
                cat=esc(pr["category"]),
            )
        )

    def child_section(title, table, col, key):
        lines.append("")
        lines.append(f"-- ========== {title} ==========")
        for pr in data["products"]:
            pid = pr["id"]
            for i, val in enumerate(pr.get(key, []) or []):
                lines.append(
                    f"INSERT INTO {table} (product_id, sort_order, {col}) VALUES ({pid}, {i}, '{esc(val)}');"
                )

    child_section("product_ingredients", "product_ingredients", "ingredient", "ingredients")
    child_section("product_use_for", "product_use_for", "use_for", "useFor")
    child_section("product_images", "product_images", "image_url", "images")

    SQL_PATH.parent.mkdir(parents=True, exist_ok=True)
    SQL_PATH.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print("Wrote", SQL_PATH, "(", len(lines), "lines )")


if __name__ == "__main__":
    main()
