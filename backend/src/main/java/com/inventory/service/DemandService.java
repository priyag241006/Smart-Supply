package com.inventory.service;

import com.inventory.model.Product;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class DemandService {

    private final InventoryService inventoryService;

    public DemandService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public Map<String, Object> getForecast(String productId, int windowDays) {
        TreeMap<LocalDate, Integer> log = inventoryService.getSalesForProduct(productId);
        Product p = inventoryService.getProduct(productId);
        List<Map<String, Object>> dailyData = new ArrayList<>();
        int totalSold = 0;
        for (int i = windowDays - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            int sold = log.getOrDefault(date, 0);
            totalSold += sold;
            dailyData.add(Map.of("date", date.toString(), "sold", sold));
        }
        double avgPerDay = (double) totalSold / windowDays;
        int reorderQty = (int) Math.ceil(avgPerDay * 14);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("productId", productId);
        result.put("productName", p != null ? p.getName() : "Unknown");
        result.put("windowDays", windowDays);
        result.put("avgPerDay", Math.round(avgPerDay * 100.0) / 100.0);
        result.put("totalSold", totalSold);
        result.put("reorderQty", reorderQty);
        result.put("currentStock", p != null ? p.getQuantity() : 0);
        result.put("dailyData", dailyData);
        return result;
    }

    public List<Map<String, Object>> getDailySalesSummary(int days) {
        Map<LocalDate, Integer> totals = new TreeMap<>();
        LocalDate start = LocalDate.now().minusDays(days - 1);
        for (int i = 0; i < days; i++) totals.put(start.plusDays(i), 0);
        for (Product p : inventoryService.getAllProducts()) {
            TreeMap<LocalDate, Integer> log = inventoryService.getSalesForProduct(p.getId());
            SortedMap<LocalDate, Integer> window = log.subMap(start, true, LocalDate.now(), true);
            window.forEach((date, qty) -> totals.merge(date, qty, Integer::sum));
        }
        List<Map<String, Object>> result = new ArrayList<>();
        totals.forEach((date, total) ->
            result.add(Map.of("date", date.toString(), "total", total))
        );
        return result;
    }

    public List<Map<String, Object>> getTopSellingProducts(int windowDays, int topN) {
        List<Map<String, Object>> list = new ArrayList<>();
        LocalDate from = LocalDate.now().minusDays(windowDays);
        for (Product p : inventoryService.getAllProducts()) {
            TreeMap<LocalDate, Integer> log = inventoryService.getSalesForProduct(p.getId());
            SortedMap<LocalDate, Integer> window = log.subMap(from, true, LocalDate.now(), true);
            int total = window.values().stream().mapToInt(Integer::intValue).sum();
            list.add(new LinkedHashMap<>(Map.of(
                "productId", p.getId(),
                "productName", p.getName(),
                "category", p.getCategory(),
                "totalSold", total,
                "currentStock", p.getQuantity()
            )));
        }
        list.sort((a, b) -> (int) b.get("totalSold") - (int) a.get("totalSold"));
        return list.subList(0, Math.min(topN, list.size()));
    }
}