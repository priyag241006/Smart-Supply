package com.inventory.ds;

import com.inventory.model.Product;
import java.util.ArrayList;
import java.util.List;

public class ProductHashMap {

    private static final int DEFAULT_CAPACITY = 64;
    private static final double LOAD_FACTOR = 0.75;

    private static class Entry {
        String key;
        Product value;
        Entry next;
        Entry(String key, Product value) {
            this.key = key;
            this.value = value;
        }
    }

    private Entry[] buckets;
    private int size;
    private int capacity;

    public ProductHashMap() {
        this.capacity = DEFAULT_CAPACITY;
        this.buckets = new Entry[capacity];
        this.size = 0;
    }

    private int hash(String key) {
        int h = 0;
        for (char c : key.toCharArray()) h = 31 * h + c;
        return Math.abs(h) % capacity;
    }

    public void put(String key, Product value) {
        if ((double) size / capacity >= LOAD_FACTOR) resize();
        int idx = hash(key);
        Entry cur = buckets[idx];
        while (cur != null) {
            if (cur.key.equals(key)) { cur.value = value; return; }
            cur = cur.next;
        }
        Entry entry = new Entry(key, value);
        entry.next = buckets[idx];
        buckets[idx] = entry;
        size++;
    }

    public Product get(String key) {
        int idx = hash(key);
        Entry cur = buckets[idx];
        while (cur != null) {
            if (cur.key.equals(key)) return cur.value;
            cur = cur.next;
        }
        return null;
    }

    public boolean remove(String key) {
        int idx = hash(key);
        Entry cur = buckets[idx];
        Entry prev = null;
        while (cur != null) {
            if (cur.key.equals(key)) {
                if (prev == null) buckets[idx] = cur.next;
                else prev.next = cur.next;
                size--;
                return true;
            }
            prev = cur;
            cur = cur.next;
        }
        return false;
    }

    public boolean containsKey(String key) { return get(key) != null; }
    public int size() { return size; }

    public List<Product> values() {
        List<Product> list = new ArrayList<>();
        for (Entry bucket : buckets) {
            Entry cur = bucket;
            while (cur != null) { list.add(cur.value); cur = cur.next; }
        }
        return list;
    }

    private void resize() {
        capacity *= 2;
        Entry[] newBuckets = new Entry[capacity];
        for (Entry bucket : buckets) {
            Entry cur = bucket;
            while (cur != null) {
                int idx = hash(cur.key);
                Entry next = cur.next;
                cur.next = newBuckets[idx];
                newBuckets[idx] = cur;
                cur = next;
            }
        }
        buckets = newBuckets;
    }
}