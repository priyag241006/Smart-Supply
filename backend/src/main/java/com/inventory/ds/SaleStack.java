package com.inventory.ds;

import java.util.ArrayList;
import java.util.List;

public class SaleStack {

    public static class SaleRecord {
        public final String productId;
        public final String productName;
        public final int qtySold;
        public final int prevQuantity;
        public final long timestamp;

        public SaleRecord(String productId, String productName,
                          int qtySold, int prevQuantity) {
            this.productId = productId;
            this.productName = productName;
            this.qtySold = qtySold;
            this.prevQuantity = prevQuantity;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private final List<SaleRecord> stack;
    private final int maxSize;

    public SaleStack(int maxSize) {
        this.stack = new ArrayList<>();
        this.maxSize = maxSize;
    }

    public void push(SaleRecord record) {
        if (stack.size() >= maxSize) stack.remove(0);
        stack.add(record);
    }

    public SaleRecord peek() {
        if (stack.isEmpty()) return null;
        return stack.get(stack.size() - 1);
    }

    public SaleRecord pop() {
        if (stack.isEmpty()) return null;
        return stack.remove(stack.size() - 1);
    }

    public boolean isEmpty() { return stack.isEmpty(); }
    public int size() { return stack.size(); }

    public List<SaleRecord> getHistory() {
        List<SaleRecord> result = new ArrayList<>(stack);
        java.util.Collections.reverse(result);
        return result;
    }
}