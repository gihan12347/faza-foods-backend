const fs = require("fs");
const path = require("path");

const root = path.join(__dirname, "..");
const jsonPath = path.join(root, "src/main/resources/data/products.json");
const sqlPath = path.join(root, "src/main/resources/db/products.sql");

const data = JSON.parse(fs.readFileSync(jsonPath, "utf8"));

function esc(s) {
  return String(s == null ? "" : s).replace(/\\/g, "\\\\").replace(/'/g, "''");
}

const lines = [
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
];

for (const [table, col] of [
  ["product_ingredients", "ingredient"],
  ["product_use_for", "use_for"],
  ["product_images", "image_url"],
]) {
  lines.push(
    `CREATE TABLE ${table} (`,
    "  product_id BIGINT NOT NULL,",
    "  sort_order INT NOT NULL,",
    `  ${col} VARCHAR(500) NOT NULL,`,
    "  PRIMARY KEY (product_id, sort_order),",
    `  CONSTRAINT fk_${table}_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE`,
    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;",
    ""
  );
}

lines.push("-- ========== products ==========");
for (const pr of data.products) {
  const bs = pr.isBestSeller ? 1 : 0;
  lines.push(
    `INSERT INTO products (id, name, description, price, original_price, is_best_seller, weight, image, how_to_use, category) VALUES (${pr.id}, '${esc(pr.name)}', '${esc(pr.description || "")}', ${pr.price}, ${pr.originalPrice}, ${bs}, '${esc(pr.weight || "")}', '${esc(pr.image || "")}', '${esc(pr.howToUse || "")}', '${esc(pr.category)}');`
  );
}

function childSection(title, table, col, key) {
  lines.push("");
  lines.push(`-- ========== ${title} ==========`);
  for (const pr of data.products) {
    const list = pr[key] || [];
    list.forEach((val, i) => {
      lines.push(
        `INSERT INTO ${table} (product_id, sort_order, ${col}) VALUES (${pr.id}, ${i}, '${esc(val)}');`
      );
    });
  }
}

childSection("product_ingredients", "product_ingredients", "ingredient", "ingredients");
childSection("product_use_for", "product_use_for", "use_for", "useFor");
childSection("product_images", "product_images", "image_url", "images");

fs.mkdirSync(path.dirname(sqlPath), { recursive: true });
fs.writeFileSync(sqlPath, lines.join("\n") + "\n", "utf8");
console.log("Wrote", sqlPath, "lines:", lines.length);
