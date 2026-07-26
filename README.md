# Smart Supply — Smart Inventory Management System

---

## Problem Statement

Traditional grocery inventory systems rely on manual tracking — spreadsheets, physical counts, and gut-feel reordering. This causes:

- Stockouts — popular products run out, sales are lost
- Overstocking — products expire before selling, waste increases
- No prediction — managers react to problems, never prevent them
- Slow lookups — scanning entire inventory for every query

Smart Supply solves all of this using purpose-built Data Structures that make every operation fast, automatic, and intelligent.

---

## Solution Overview

A Optimized Smart Inventory Management System where every core feature is powered by a hand-built DSA structure in Java. The system gives store managers:

- Live inventory dashboard with search, filter and sort
- Real-time billing with instant undo
- Three independent alert systems running simultaneously
- Automatic removal of expired products
- Sales analytics with demand forecasting
- Velocity-based stockout prediction

---

## System Architecture

```
+------------------------------------------+
|            React Frontend                |
|  Dashboard | Billing | Alerts | Analytics |
+------------------+-----------------------+
                   | HTTP / REST (Axios)
+------------------v-----------------------+
|         Spring Boot REST API             |
|          (Java - Port 8080)              |
+------------------+-----------------------+
                   |
+------------------v-----------------------+
|            DSA Core (Pure Java)          |
|                                          |
|  ProductHashMap    MinHeap (Stock)       |
|  MinHeap (Expiry)  SaleStack             |
|  AVLTree           TreeMap (built-in)    |
|  ArrayList + Sliding Window              |
+------------------------------------------+
```

---

## Data Structures Used

### 1. Custom HashMap — Product Storage
**File:** `ProductHashMap.java`

All 28+ products stored with Product ID as key. Uses separate chaining for collision handling and auto-resizes when load factor exceeds 0.75.

```java
// O(1) average — direct key access, no iteration
hashMap.put("P001", product);   // Insert
hashMap.get("P001");            // Search
hashMap.remove("P001");         // Delete
```

**Why HashMap?** Any other structure (ArrayList, LinkedList) would require O(n) scan for every product lookup. HashMap gives O(1) — critical for real-time billing.

---

### 2. MinHeap (Stock) — Low Stock Alerts
**File:** `MinHeap.java`

Products ordered by quantity. Lowest stock always at root. Built with `bubbleUp` and `heapifyDown` operations.

```java
MinHeap<Product> stockHeap = new MinHeap<>(
    Comparator.comparingInt(Product::getQuantity)
);
// O(1) — instantly know worst-off product
Product critical = stockHeap.peek();
```

**Why MinHeap?** Without it, finding lowest stock = O(n) scan every time. With MinHeap = O(1) peek. Alert fires automatically when quantity drops below threshold.

---

### 3. MinHeap (Expiry) — Expiry Alerts + Auto Removal
**File:** `MinHeap.java` *(same class, different Comparator)*

Products ordered by expiry date. Nearest expiry always at root.

```java
MinHeap<Product> expiryHeap = new MinHeap<>(
    Comparator.comparing(Product::getExpiryDate)
);
// Auto-removal runs every 60 seconds
@Scheduled(fixedRate = 60000)
public void autoRemoveExpiredProducts() { ... }
```

**Auto-removal logic:** Every 60 seconds, system checks if any product's expiry date is before today. If yes, logs the removal, deletes from HashMap, both MinHeaps and AVL Tree simultaneously, then notifies the manager via dashboard toast notification.

---

### 4. Stack — Undo Last Sale
**File:** `SaleStack.java`

Every billing transaction pushed to a custom LIFO Stack storing previous state. Undo restores stock in O(1).

```java
// On sale
saleStack.push(new SaleRecord(productId, name, qtySold, prevQuantity));

// On undo — O(1) restore
SaleRecord record = saleStack.pop();
product.setQuantity(record.prevQuantity);
```

**Why Stack?** LIFO perfectly matches undo semantics — last action is always first to reverse. Stores last 50 transactions.

---

### 5. AVL Tree — Range Queries
**File:** `AVLTree.java`

Self-balancing BST with left/right rotations maintaining balance factor. Used for price and stock range queries.

```java
// Find all products priced Rs.20 to Rs.200 — O(log n + k)
List<Product> result = priceTree.rangeQuery(20.0, 200.0);

// Find all products with stock 10 to 50
List<Product> result = stockTree.rangeQuery(10.0, 50.0);
```

**Why AVL over plain BST?** Plain BST can degrade to O(n) in worst case. AVL self-balances after every insert/delete using rotations — guarantees O(log n).

---

### 6. TreeMap — Date-Range Sales Analytics
**File:** `InventoryService.java`, `DemandService.java`

Java's built-in TreeMap stores daily sales keyed by LocalDate. Sorted automatically. Range queries use `subMap()`.

```java
// O(log n + k) — get sales between two dates
SortedMap<LocalDate, Integer> window =
    salesLog.subMap(from, true, LocalDate.now(), true);
```

**Why TreeMap?** Keeps dates sorted automatically. `subMap()` gives efficient range retrieval without scanning all entries — essential for analytics.

---

### 7. ArrayList + Sliding Window — Velocity and Stockout Prediction
**File:** `AlertService.java`

The star feature. Each product has an ArrayList of daily sales. A 7-day sliding window computes velocity. Dividing current stock by velocity predicts exact days to stockout.

```java
// Sliding window — O(k)
double velocity = totalSoldInWindow / windowDays;
double daysToStockout = currentStock / velocity;

// Example:
// Stock = 30, Velocity = 15/day → runs out in 2 days → CRITICAL
// Stock = 10, Velocity = 1/day  → runs out in 10 days → No alert
```

**Why this matters:** Plain low-stock alert treats 10 units the same regardless of sales speed. Velocity detection knows the urgency. A product with 30 units selling 15/day is more critical than one with 5 units selling 0.1/day.

---

### 8. Moving Average — Demand Forecasting
**File:** `DemandService.java`

Same ArrayList used for demand prediction. 7-day moving average shown on analytics chart. Suggested reorder quantity = average daily sales x 14 days.

```java
double avgPerDay = totalSold / windowDays;
int reorderQty = (int) Math.ceil(avgPerDay * 14);
```

---

## DSA Complexity Summary

| Feature | Structure | Time Complexity | Built |
|---------|-----------|----------------|-------|
| Product lookup | HashMap | O(1) avg | Custom |
| Low stock alert | MinHeap (qty) | O(1) peek, O(log n) update | Custom |
| Expiry alert | MinHeap (date) | O(1) peek, O(log n) update | Custom |
| Auto-remove expired | MinHeap + HashMap | O(log n) | Custom |
| Undo sale | Stack | O(1) push/pop | Custom |
| Range queries | AVL Tree | O(log n) | Custom |
| Date-range sales | TreeMap | O(log n + k) | Built-in |
| Velocity/stockout | ArrayList window | O(k) | Built-in |
| Demand forecast | Moving average | O(k) | Algorithm |

---

## Features

### Dashboard
- Live inventory table — 28+ products, 7 categories
- Search by name, ID or category
- Filter — All / Low Stock / Out of Stock / Healthy
- Sort by name, price or quantity
- Add new product via modal form
- Delete product — removed from all DSA structures simultaneously
- Stat cards — total products, alerts, low stock count, inventory value
- Critical alert strip with pulsing indicators
- Auto-refresh every 30 seconds

### Billing
- Select product — instant HashMap O(1) lookup
- +/- quantity controls with live price calculation
- Confirm sale — updates HashMap, both MinHeaps, AVL Tree, TreeMap and Stack
- Undo last sale — Stack O(1) restore
- Full transaction history panel

### Alerts (3 Independent Systems)
- Fast Depletion — velocity-based, shows selling rate and days to stockout
- Low Stock — MinHeap powered, shows deficit vs threshold
- Expiry — MinHeap powered, shows exact days left
- Severity levels — CRITICAL / WARNING / INFO
- Auto-removal notification when product expires

### Analytics
- 14-day sales bar chart
- 7-day moving average overlay (demand forecast line)
- Top 5 selling products with visual bars
- All powered by TreeMap date-range queries

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| DSA Core | Pure Java 25 — hand-built structures |
| Backend | Spring Boot 3.4.0 |
| Build Tool | Maven 3.9.15 |
| Frontend | React 18 + Vite |
| Charts | Recharts |
| Icons | Lucide React |
| HTTP | Axios |
| Routing | React Router v6 |
| Data | In-memory (DSA structures are the database) |

---

## Project Structure

```
smart-inventory-system/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/inventory/
│       ├── SmartInventoryApplication.java
│       ├── model/
│       │   └── Product.java
│       ├── ds/
│       │   ├── ProductHashMap.java       <- Custom HashMap
│       │   ├── MinHeap.java             <- Custom MinHeap (used twice)
│       │   ├── SaleStack.java           <- Custom Stack
│       │   └── AVLTree.java             <- Custom AVL Tree
│       ├── service/
│       │   ├── InventoryService.java    <- Core logic + all DSA wired
│       │   ├── AlertService.java        <- 3 alert systems
│       │   └── DemandService.java       <- Analytics + forecasting
│       └── controller/
│           └── InventoryController.java <- REST endpoints
└── frontend/
    └── src/
        ├── pages/
        │   ├── Dashboard.jsx
        │   ├── Billing.jsx
        │   ├── Alerts.jsx
        │   └── Analytics.jsx
        ├── components/
        │   └── Sidebar.jsx
        └── api/
            └── inventory.js
```

---

## How to Run

### Prerequisites
- Java 17 or above (tested on Java 25)
- Maven 3.9 or above
- Node.js 18 or above

### Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
# Runs on http://localhost:8080
```

### Frontend
```bash
cd frontend
npm install
npm run dev
# Runs on http://localhost:5173
```

---

## Prototype Video

https://drive.google.com/file/d/1rWKcrnQAR7uIp8y6emI3YU04mKAcId6t/view?usp=sharing

---

## Real-World Impact

| Problem | Solution | DSA Behind It |
|---------|----------|---------------|
| Slow product lookup | O(1) direct access | HashMap |
| Missing low stock warning | Instant heap peek | MinHeap |
| Products expiring unnoticed | Auto-removal every 60s | MinHeap + Scheduler |
| Billing mistakes | Undo in O(1) | Stack |
| No demand insights | Moving average forecast | ArrayList + Algorithm |
| Reactive not predictive | Velocity stockout prediction | Sliding Window |
