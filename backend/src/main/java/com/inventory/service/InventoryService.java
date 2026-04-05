package com.inventory.service;

import com.inventory.ds.*;
import com.inventory.model.Product;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class InventoryService {

    private final ProductHashMap hashMap = new ProductHashMap();
    private final MinHeap<Product> stockHeap = new MinHeap<>(
            Comparator.comparingInt(Product::getQuantity));
    private final MinHeap<Product> expiryHeap = new MinHeap<>(
            Comparator.comparing(Product::getExpiryDate));
    private final SaleStack saleStack = new SaleStack(50);
    private final AVLTree priceTree = new AVLTree();
    private final AVLTree stockTree = new AVLTree();
    private final Map<String, TreeMap<LocalDate, Integer>> salesLog = new HashMap<>();

    public InventoryService() {
        List<Product> samples = Arrays.asList(
            new Product("P001", "Amul Milk 500ml",     "Dairy",      120, 20, 28.0,  LocalDate.now().plusDays(3)),
            new Product("P002", "Britannia Bread",     "Bakery",      45, 15, 42.0,  LocalDate.now().plusDays(5)),
            new Product("P003", "Fortune Rice 5kg",    "Grains",      80, 10, 285.0, LocalDate.now().plusMonths(6)),
            new Product("P004", "Maggi Noodles 70g",   "Snacks",     200, 30, 14.0,  LocalDate.now().plusMonths(8)),
            new Product("P005", "Tropicana OJ 1L",     "Beverages",   18, 15, 99.0,  LocalDate.now().plusDays(7)),
            new Product("P006", "Haldirams Bhujia",    "Snacks",      60, 20, 120.0, LocalDate.now().plusMonths(3)),
            new Product("P007", "Amul Butter 500g",    "Dairy",       12, 15, 260.0, LocalDate.now().plusDays(10)),
            new Product("P008", "Tata Salt 1kg",       "Essentials", 150, 25, 22.0,  LocalDate.now().plusMonths(12)),
            new Product("P009", "Parle-G Biscuits",    "Snacks",       8, 20, 10.0,  LocalDate.now().plusMonths(5)),
            new Product("P010", "Mother Dairy Curd",   "Dairy",       35, 20, 54.0,  LocalDate.now().plusDays(2)),
            new Product("P011", "Aashirvaad Atta 5kg", "Grains",      55, 10, 290.0, LocalDate.now().plusMonths(4)),
            new Product("P012", "Coca-Cola 2L",        "Beverages",   90, 20, 95.0,  LocalDate.now().plusMonths(9))
        );
        samples.forEach(this::addProduct);

        Random rand = new Random(42);
        for (Product p : samples) {
            TreeMap<LocalDate, Integer> log = salesLog.computeIfAbsent(
                    p.getId(), k -> new TreeMap<>());
            for (int i = 14; i >= 1; i--) {
                int qty = 2 + rand.nextInt(p.getId().equals("P009") ? 12 : 4);
                log.put(LocalDate.now().minusDays(i), qty);
            }
        }
    }

    public void addProduct(Product p) {
        hashMap.put(p.getId(), p);
        stockHeap.insert(p);
        expiryHeap.insert(p);
        priceTree.insert(p.getPrice(), p);
        stockTree.insert(p.getQuantity(), p);
        salesLog.putIfAbsent(p.getId(), new TreeMap<>());
    }

    public Product getProduct(String id) { return hashMap.get(id); }
    public List<Product> getAllProducts() { return hashMap.values(); }

    public boolean deleteProduct(String id) {
        Product p = hashMap.get(id);
        if (p == null) return false;
        hashMap.remove(id);
        stockHeap.remove(p);
        expiryHeap.remove(p);
        priceTree.remove(p.getPrice());
        stockTree.remove(p.getQuantity());
        return true;
    }

    public Map<String, Object> processSale(String productId, int qtySold) {
        Product p = hashMap.get(productId);
        Map<String, Object> result = new HashMap<>();
        if (p == null) {
            result.put("success", false);
            result.put("message", "Product not found");
            return result;
        }
        if (p.getQuantity() < qtySold) {
            result.put("success", false);
            result.put("message", "Insufficient stock. Available: " + p.getQuantity());
            return result;
        }
        int prev = p.getQuantity();
        stockHeap.remove(p);
        stockTree.remove(prev);
        p.setQuantity(prev - qtySold);
        stockHeap.insert(p);
        stockTree.insert(p.getQuantity(), p);
        saleStack.push(new SaleStack.SaleRecord(p.getId(), p.getName(), qtySold, prev));
        salesLog.get(productId).merge(LocalDate.now(), qtySold, Integer::sum);
        result.put("success", true);
        result.put("product", p);
        result.put("message", "Sale processed: " + qtySold + "x " + p.getName());
        return result;
    }

    public Map<String, Object> undoLastSale() {
        SaleStack.SaleRecord record = saleStack.pop();
        Map<String, Object> result = new HashMap<>();
        if (record == null) {
            result.put("success", false);
            result.put("message", "Nothing to undo");
            return result;
        }
        Product p = hashMap.get(record.productId);
        if (p != null) {
            stockHeap.remove(p);
            stockTree.remove(p.getQuantity());
            p.setQuantity(record.prevQuantity);
            stockHeap.insert(p);
            stockTree.insert(p.getQuantity(), p);
            TreeMap<LocalDate, Integer> log = salesLog.get(record.productId);
            if (log != null && log.containsKey(LocalDate.now())) {
                int updated = log.get(LocalDate.now()) - record.qtySold;
                if (updated <= 0) log.remove(LocalDate.now());
                else log.put(LocalDate.now(), updated);
            }
        }
        result.put("success", true);
        result.put("record", Map.of(
            "productId", record.productId,
            "productName", record.productName,
            "qtySold", record.qtySold,
            "timestamp", record.timestamp
        ));
        result.put("message", "Undid sale of " + record.qtySold + "x " + record.productName);
        return result;
    }

    public MinHeap<Product> getStockHeap()  { return stockHeap; }
    public MinHeap<Product> getExpiryHeap() { return expiryHeap; }
    public SaleStack getSaleStack()         { return saleStack; }

    public List<Product> getByPriceRange(double lo, double hi) { return priceTree.rangeQuery(lo, hi); }
    public List<Product> getByStockRange(double lo, double hi) { return stockTree.rangeQuery(lo, hi); }

    public Map<String, TreeMap<LocalDate, Integer>> getSalesLog() { return salesLog; }
    public TreeMap<LocalDate, Integer> getSalesForProduct(String productId) {
        return salesLog.getOrDefault(productId, new TreeMap<>());
    }
}