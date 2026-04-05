package com.inventory.service;

import com.inventory.model.Product;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class AlertService {

    private final InventoryService inventoryService;

    public AlertService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public List<Map<String, Object>> getLowStockAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        for (Product p : inventoryService.getAllProducts()) {
            if (p.getQuantity() <= p.getThreshold()) {
                Map<String, Object> alert = new LinkedHashMap<>();
                alert.put("type", "LOW_STOCK");
                alert.put("severity", p.getQuantity() == 0 ? "CRITICAL" : "WARNING");
                alert.put("productId", p.getId());
                alert.put("productName", p.getName());
                alert.put("quantity", p.getQuantity());
                alert.put("threshold", p.getThreshold());
                alert.put("message", p.getName() + " is low (" + p.getQuantity() + " left)");
                alerts.add(alert);
            }
        }
        alerts.sort(Comparator.comparingInt(a -> (int) a.get("quantity")));
        return alerts;
    }

    public List<Map<String, Object>> getExpiryAlerts(int withinDays) {
        List<Map<String, Object>> alerts = new ArrayList<>();
        LocalDate cutoff = LocalDate.now().plusDays(withinDays);
        for (Product p : inventoryService.getAllProducts()) {
            if (p.getExpiryDate() != null && !p.getExpiryDate().isAfter(cutoff)) {
                long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(
                        LocalDate.now(), p.getExpiryDate());
                Map<String, Object> alert = new LinkedHashMap<>();
                alert.put("type", "EXPIRY");
                alert.put("severity", daysLeft <= 1 ? "CRITICAL" : daysLeft <= 3 ? "WARNING" : "INFO");
                alert.put("productId", p.getId());
                alert.put("productName", p.getName());
                alert.put("expiryDate", p.getExpiryDate().toString());
                alert.put("daysLeft", daysLeft);
                alert.put("message", p.getName() + " expires in " + daysLeft + " day(s)");
                alerts.add(alert);
            }
        }
        alerts.sort(Comparator.comparingLong(a -> (long) a.get("daysLeft")));
        return alerts;
    }

    public List<Map<String, Object>> getVelocityAlerts(int windowDays, double thresholdDays) {
        List<Map<String, Object>> alerts = new ArrayList<>();
        for (Product p : inventoryService.getAllProducts()) {
            double velocity = computeVelocity(p.getId(), windowDays);
            if (velocity <= 0) continue;
            double daysToStockout = p.getQuantity() / velocity;
            if (daysToStockout <= thresholdDays) {
                Map<String, Object> alert = new LinkedHashMap<>();
                alert.put("type", "VELOCITY");
                alert.put("severity", daysToStockout <= 1 ? "CRITICAL" : "WARNING");
                alert.put("productId", p.getId());
                alert.put("productName", p.getName());
                alert.put("quantity", p.getQuantity());
                alert.put("velocity", Math.round(velocity * 10.0) / 10.0);
                alert.put("daysToStockout", Math.round(daysToStockout * 10.0) / 10.0);
                alert.put("message", p.getName() + " will run out in ~"
                        + Math.round(daysToStockout) + " day(s) at current rate");
                alerts.add(alert);
            }
        }
        alerts.sort(Comparator.comparingDouble(a -> (double) a.get("daysToStockout")));
        return alerts;
    }

    public double computeVelocity(String productId, int windowDays) {
        TreeMap<LocalDate, Integer> log = inventoryService.getSalesForProduct(productId);
        if (log.isEmpty()) return 0;
        LocalDate from = LocalDate.now().minusDays(windowDays);
        SortedMap<LocalDate, Integer> window = log.subMap(from, true, LocalDate.now(), true);
        int total = window.values().stream().mapToInt(Integer::intValue).sum();
        return (double) total / windowDays;
    }

    public Map<String, Object> getAllAlerts() {
        Map<String, Object> all = new LinkedHashMap<>();
        all.put("lowStock", getLowStockAlerts());
        all.put("expiry", getExpiryAlerts(7));
        all.put("velocity", getVelocityAlerts(7, 3.0));
        all.put("totalCount",
                getLowStockAlerts().size()
                + getExpiryAlerts(7).size()
                + getVelocityAlerts(7, 3.0).size());
        return all;
    }
}