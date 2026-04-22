# Smart Supply - Smart Inventory Management System

A full-stack inventory management system built for grocery store managers. Every core operation is powered by a hand-built DSA structure in Java, giving real-time visibility into stock levels, sales, alerts, and demand prediction.

---

## Problem

Grocery stores lose money in two ways simultaneously — products run out of stock causing lost sales, and products expire on shelves causing wastage — because inventory is tracked manually with no real-time visibility.

---

## Solution

A live dashboard where a store manager can monitor stock, process sales, receive automatic alerts, and predict future demand. Every operation runs at optimal time complexity using custom-built data structures.

---

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.4, Maven
- **Frontend:** React, Vite, JavaScript
- **Data Structures:** Custom-built HashMap, MinHeap, AVL Tree, Stack, TreeMap, ArrayList

---

## Features

### Product Storage and Lookup
Products are stored in a custom HashMap using Product ID as the key. Add, search, update, and delete all run in O(1) average time.

### Billing and Sale Processing
Enter a Product ID to process a sale. The system fetches the product instantly, deducts quantity, logs the transaction with a timestamp, and updates all relevant data structures. Full transaction in O(log n).

### Undo Last Sale
A custom Stack stores recent sale operations. If a billing mistake happens, the manager can reverse the last transaction in O(1).

### Low Stock Alerts
A MinHeap ordered by quantity keeps the lowest-stock product at the top at all times. Alerts fire automatically when any product crosses the threshold, without scanning the full inventory.

### Expiry Alerts
A separate MinHeap ordered by expiry date surfaces products expiring within a configurable window. The manager can then discount or remove items before they expire.

### Date-Range Sales Query
All sales are stored in a TreeMap keyed by date. The manager can query sales between any two dates in O(log n + k) using subMap, far faster than scanning a plain list.

### Price and Stock Range Queries
A self-balancing AVL Tree supports queries like "show all products with stock between 10 and 50 units" or "all products priced between Rs. 20 and Rs. 100" in O(log n).

### Demand Prediction
Daily sales are stored in an ArrayList. A sliding window moving average over the last 7 days predicts next-day demand and generates reorder suggestions to prevent both overstocking and understocking.

### Velocity-Based Stockout Prediction
Rather than treating all stock the same, the system computes how fast each product is selling and estimates days until stockout.

```
velocity = sum(sales[last 7 days]) / 7
days_to_stockout = current_stock / velocity
```

30 units selling 1 per day gives 30 days — no urgency. 30 units selling 15 per day gives 2 days — critical alert fires immediately.

---

## DSA Summary

| Structure | Purpose | Time Complexity |
|---|---|---|
| HashMap | Product storage and lookup | O(1) |
| Stack | Undo last sale | O(1) |
| MinHeap (quantity) | Low stock alerts | O(log n) insert, O(1) peek |
| MinHeap (expiry) | Expiry alerts | O(log n) insert, O(1) peek |
| AVL Tree | Price and stock range queries | O(log n) |
| TreeMap | Date-range sales history | O(log n + k) |
| ArrayList | Demand prediction sliding window | O(k) |

---

## Project Structure

```
smart-inventory-system/
├── backend/
│   └── src/main/java/com/inventory/
│       ├── controller/     InventoryController.java
│       ├── ds/             AVLTree, MinHeap, ProductHashMap, SaleStack
│       ├── model/          Product.java
│       └── service/        AlertService, DemandService, InventoryService
└── frontend/
    └── src/
        ├── api/            inventory.js
        └── pages/          Dashboard, Billing, Alerts, Analytics
```

---

## Built for

Buffer DSA Hackathon
