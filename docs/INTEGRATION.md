# Vanilla Coffee — Two-Module Integration Guide

This repository contains **two related JavaFX apps** that share the same business domain but are **not merged into one runnable program** (duplicate class names and different SQL would break a single build).

## Module 1 — Customer & Staff Portal (your work)

| Item | Location |
|------|----------|
| Run | `src/.../Main.java` |
| IntelliJ config | `Vanilla Coffee System` |
| Users | Customers + employees (login) |
| Features | Home, menu, place order, payments, reservations, delivery tracking, staff delivery updates |

## Module 2 — Admin & Inventory (partner work)

| Item | Location |
|------|----------|
| Run | `VanillaCoffeeSystem/.../Main.java` |
| Users | Admin / back-office (no login in partner app) |
| Features | Products, orders CRUD, suppliers, warehouse, stock, purchase analytics |

Both should use database **`vanilla_db`** on `localhost:3306`.

---

## Schema compatibility

Partner Java code has been **updated to use your canonical schema** (`sql/1221124.sql`):

| Old (partner) | New (your schema) |
|---------------|-------------------|
| `employee` + `employee_name` | `Employee` + `first_name`, `last_name` |
| `branch` | `Branches` |
| `customer` | `Customers` |
| `orders` | `Orders` |
| `order_items` | `Order_Items` |
| `employee_order` | `Employee_Order` |
| `payment` | `Payment` |
| `supplier` | `Suppliers` |
| `warehouse_inventory` | `Warehouse_Inventory` |
| `stock_movement` | `Stock_Movement` |

Run optional demo seed for partner analytics tabs:

```sql
SOURCE sql/migrate_partner_inventory_seed.sql;
```

---

## Advanced SQL — who uses what?

### Your module (customer portal)

Mostly **JOINs** and simple aggregates:

- `JOIN` — orders + branches, payments + orders, delivery + orders, menu + branch_product
- `SUM` + `COALESCE` — total paid in payment history
- `NOW()` — delivery status timestamp
- Prepared `INSERT` / `UPDATE` in transactions (place order)

**No** `GROUP BY`, **no** subqueries, **no** `HAVING` in your customer-facing code.

### Partner module (admin / analytics)

**Advanced queries** (good for course report):

| Screen | SQL features |
|--------|----------------|
| Low Stock Alert | 4-table `JOIN`, computed column, `ORDER BY` |
| Employee Performance | `JOIN`, `GROUP BY`, `COUNT`, `HAVING` |
| Warehouse Utilization | `JOIN`, `SUM`, `ROUND`, `GROUP BY` |
| Purchase Cost Analytics | `JOIN`, `DATE_FORMAT`, `SUM`, `GROUP BY` |
| Stock Movement Log | 2-table `JOIN`, `ORDER BY DESC` |
| Dashboard | `COUNT(*)` per table |

---

## How to run both (demo for submission)

### 1. Database

```sql
SOURCE sql/1221124.sql;
SOURCE sql/migrate_employee_auth.sql;
-- run other migrate_*.sql you already use
```

### 2. Customer app (IntelliJ)

- Main class: `com.example.vanillacoffeesystem.Main`
- VM options: `--module-path .../javafx-sdk-26.0.1/lib --add-modules javafx.controls,javafx.fxml`

### 3. Admin app (IntelliJ — second run configuration)

- Main class: `com.example.vanillacoffeesystem.Main` inside **VanillaCoffeeSystem** Maven module
- Same JavaFX VM options
- Update `VanillaCoffeeSystem/.../DBConnection.java` password to match yours

---

## What we did NOT merge (on purpose)

- Duplicate Java classes (`DashboardController`, `OrderController`, `ProductController`, `Branch`, `DBConnection`, …)
- Partner analytics SQL without schema alignment
- Single login flow across both modules

The **shared visual theme** (coffee browns, cream background) is aligned; each app runs separately but looks like one product family.
