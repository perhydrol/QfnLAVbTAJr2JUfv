package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private int size;
    private BSTNode root;

    @Override
    public void clear() {
        size = 0;
        root = null;
    }

    @Override
    public boolean containsKey(K key) {
        return containsKey(root, key);
    }

    private boolean containsKey(BSTNode node, K key) {
        if (node == null) {
            return false;
        }
        int cmp = node.key.compareTo(key);
        boolean ans = false;
        if (cmp < 0) {
            ans = containsKey(node.right, key);
        } else if (cmp > 0) {
            ans = containsKey(node.left, key);
        } else {
            ans = true;
        }
        return ans;
    }

    @Override
    public V get(K key) {
        return get(root, key);
    }

    private V get(BSTNode node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = node.key.compareTo(key);
        V ans = null;
        if (cmp < 0) {
            ans = get(node.right, key);
        } else if (cmp > 0) {
            ans = get(node.left, key);
        } else {
            ans = node.value;
        }
        return ans;
    }

    @Override
    public int size() {
        return size;
    }

    private boolean isLeaf(BSTNode node) {
        return node.left == null && node.right == null;
    }

    @Override
    public void put(K key, V value) {
        root = put(root, key, value);
        size += 1;
    }

    private boolean isRed(BSTNode node) {
        if (node == null) {
            return false;
        }
        return node.isRed;
    }

    private BSTNode put(BSTNode h, K k, V v) {
        if (h == null) {
            return new BSTNode(k, v, null, null, true);
        }
        int cmp = k.compareTo(h.key);
        if (cmp < 0) {
            h.left = put(h.left, k, v);
        } else if (cmp > 0) {
            h.right = put(h.right, k, v);
        } else {
            h.value = v;
        }

        if (isRed(h.right) && !isRed(h.left)) {
            h = rotateLeft(h);
        }
        if (isRed(h.left) && isRed(h.left.left)) {
            h = rotateRight(h);
        }
        if (isRed(h.left) && isRed(h.right)) {
            flipColors(h);
        }
        return h;
    }

    @Override
    public Set<K> keySet() {
        Stack<BSTNode> list = new Stack<>();
        HashSet<K> keys = new HashSet<>();
        list.push(root);
        while (!list.empty()) {
            BSTNode current = list.pop();
            if (current != null) {
                list.push(current.left);
                list.push(current.right);
                keys.add(current.key);
            }
        }
        return keys;
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        return new BSTMapIter();
    }

    public void printInOrder() {
        Stack<BSTNode> list = new Stack<>();
        list.push(root);
        while (!list.empty()) {
            BSTNode current = list.pop();
            if (current != null) {
                list.push(current.left);
                list.push(current.right);
                System.out.print(current.key + ":" + current.value + " ");
            }
        }
        System.out.println();
    }

    private void flipColors(BSTNode node) {
        node.isRed = !(node.isRed);
        if (node.left != null) {
            node.left.isRed = !(node.left.isRed);
        }
        if (node.right != null) {
            node.right.isRed = !(node.right.isRed);
        }
    }

    private void swapColor(BSTNode a, BSTNode b) {
        boolean temp = a.isRed;
        a.isRed = b.isRed;
        b.isRed = temp;
    }

    private BSTNode rotateRight(BSTNode node) {
        if (node == null) {
            return null;
        }
        BSTNode l = node.left;
        BSTNode t2 = l.right;
        l.right = node;
        node.left = t2;
        swapColor(l, node);
        return l;
    }

    private BSTNode rotateLeft(BSTNode node) {
        if (node == null) {
            return null;
        }
        BSTNode r = node.right;
        node.right = r.left;
        r.left = node;
        swapColor(r, node);
        return r;
    }

    private class BSTMapIter implements Iterator<K> {
        /**
         * Stores the current key-value pair.
         */
        private Stack<BSTNode> list;

        public BSTMapIter() {
            list = new Stack<>();
            list.push(root);
        }

        @Override
        public boolean hasNext() {
            return !list.empty();
        }

        @Override
        public K next() {
            BSTNode current = list.pop();
            if (current != null) {
                list.push(current.left);
                list.push(current.right);
            }
            if (current != null) {
                return current.key;
            } else {
                return null;
            }
        }
    }

    private class BSTNode {
        V value;
        K key;
        BSTNode left;
        BSTNode right;
        boolean isRed;

        BSTNode(K k, V v, BSTNode l, BSTNode r, boolean isR) {
            key = k;
            value = v;
            left = l;
            right = r;
            isRed = isR;
        }
    }
}
