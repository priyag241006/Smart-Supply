package com.inventory.ds;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MinHeap<T> {

    private final List<T> heap;
    private final Comparator<T> comparator;

    public MinHeap(Comparator<T> comparator) {
        this.heap = new ArrayList<>();
        this.comparator = comparator;
    }

    public void insert(T item) {
        heap.add(item);
        bubbleUp(heap.size() - 1);
    }

    public T peek() {
        if (heap.isEmpty()) return null;
        return heap.get(0);
    }

    public T extractMin() {
        if (heap.isEmpty()) return null;
        T min = heap.get(0);
        T last = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, last);
            heapifyDown(0);
        }
        return min;
    }

    public boolean remove(T item) {
        int idx = -1;
        for (int i = 0; i < heap.size(); i++) {
            if (heap.get(i).equals(item)) { idx = i; break; }
        }
        if (idx == -1) return false;
        T last = heap.remove(heap.size() - 1);
        if (idx < heap.size()) {
            heap.set(idx, last);
            bubbleUp(idx);
            heapifyDown(idx);
        }
        return true;
    }

    public int size() { return heap.size(); }
    public boolean isEmpty() { return heap.isEmpty(); }
    public List<T> getAll() { return new ArrayList<>(heap); }

    private void bubbleUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (comparator.compare(heap.get(i), heap.get(parent)) < 0) {
                swap(i, parent);
                i = parent;
            } else break;
        }
    }

    private void heapifyDown(int i) {
        int n = heap.size();
        while (true) {
            int left = 2 * i + 1;
            int right = 2 * i + 2;
            int small = i;
            if (left < n && comparator.compare(heap.get(left), heap.get(small)) < 0) small = left;
            if (right < n && comparator.compare(heap.get(right), heap.get(small)) < 0) small = right;
            if (small == i) break;
            swap(i, small);
            i = small;
        }
    }

    private void swap(int a, int b) {
        T tmp = heap.get(a);
        heap.set(a, heap.get(b));
        heap.set(b, tmp);
    }
}