package com.inventory.ds;

import com.inventory.model.Product;
import java.util.ArrayList;
import java.util.List;

public class AVLTree {

    private static class Node {
        double key;
        Product product;
        Node left, right;
        int height;

        Node(double key, Product product) {
            this.key = key;
            this.product = product;
            this.height = 1;
        }
    }

    private Node root;

    private int height(Node n) { return n == null ? 0 : n.height; }
    private int balance(Node n) { return n == null ? 0 : height(n.left) - height(n.right); }

    private void updateHeight(Node n) {
        n.height = 1 + Math.max(height(n.left), height(n.right));
    }

    private Node rotateRight(Node y) {
        Node x = y.left;
        Node T2 = x.right;
        x.right = y;
        y.left = T2;
        updateHeight(y);
        updateHeight(x);
        return x;
    }

    private Node rotateLeft(Node x) {
        Node y = x.right;
        Node T2 = y.left;
        y.left = x;
        x.right = T2;
        updateHeight(x);
        updateHeight(y);
        return y;
    }

    private Node rebalance(Node n) {
        updateHeight(n);
        int bal = balance(n);
        if (bal > 1) {
            if (balance(n.left) < 0) n.left = rotateLeft(n.left);
            return rotateRight(n);
        }
        if (bal < -1) {
            if (balance(n.right) > 0) n.right = rotateRight(n.right);
            return rotateLeft(n);
        }
        return n;
    }

    public void insert(double key, Product product) {
        root = insert(root, key, product);
    }

    private Node insert(Node n, double key, Product product) {
        if (n == null) return new Node(key, product);
        if (key < n.key) n.left = insert(n.left, key, product);
        else if (key > n.key) n.right = insert(n.right, key, product);
        else n.product = product;
        return rebalance(n);
    }

    public void remove(double key) {
        root = remove(root, key);
    }

    private Node remove(Node n, double key) {
        if (n == null) return null;
        if (key < n.key) n.left = remove(n.left, key);
        else if (key > n.key) n.right = remove(n.right, key);
        else {
            if (n.left == null) return n.right;
            if (n.right == null) return n.left;
            Node min = n.right;
            while (min.left != null) min = min.left;
            n.key = min.key;
            n.product = min.product;
            n.right = remove(n.right, min.key);
        }
        return rebalance(n);
    }

    public List<Product> rangeQuery(double lo, double hi) {
        List<Product> result = new ArrayList<>();
        rangeQuery(root, lo, hi, result);
        return result;
    }

    private void rangeQuery(Node n, double lo, double hi, List<Product> result) {
        if (n == null) return;
        if (lo < n.key) rangeQuery(n.left, lo, hi, result);
        if (lo <= n.key && n.key <= hi) result.add(n.product);
        if (hi > n.key) rangeQuery(n.right, lo, hi, result);
    }
}