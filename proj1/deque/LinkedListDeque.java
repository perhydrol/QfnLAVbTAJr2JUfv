package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Iterable<T> {
    private Node sentinel;
    private int size;

    public LinkedListDeque() {
        this.sentinel = new Node(null, null, null);
        sentinel.next = sentinel;
        sentinel.previous = sentinel;
        size = 0;
    }

    public Iterator<T> iterator() {
        return new MyIterator();
    }

    private Node replaceNode(Node old_node, Node new_node) {
        Node previous_node = old_node.previous;
        Node next_node = old_node.next;

        previous_node.next = new_node;
        new_node.previous = previous_node;

        new_node.next = next_node;
        next_node.previous = new_node;

        return new_node;
    }

    public void addFirst(T item) {
        Node new_node = new Node(item, sentinel.next, sentinel);
        Node old_first = sentinel.next;
        sentinel.next = new_node;
        old_first.previous = new_node;
        size++;
    }

    public void addLast(T item) {
        Node new_node = new Node(item, sentinel, sentinel.previous);
        Node old_last = sentinel.previous;
        sentinel.previous = new_node;
        old_last.next = new_node;
        size++;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        Node current = sentinel.next;
        while (current != sentinel) {
            System.out.print(current.item + " ");
            current = current.next;
        }
        System.out.println();
    }

    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        Node old_first = sentinel.next;
        Node new_first = old_first.next;
        sentinel.next = new_first;
        new_first.previous = sentinel;
        size--;
        return old_first.item;
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        }
        Node old_last = sentinel.previous;
        Node new_last = old_last.previous;
        sentinel.previous = new_last;
        new_last.next = sentinel;
        size--;
        return old_last.item;
    }

    public T get(int index) {
        Node current = sentinel.next;
        if (index >= size) {
            return null;
        }
        while (current != sentinel && index > 0) {
            current = current.next;
            index--;
        }
        return current.item;
    }

    private T getRecursivePoint(int index, Node point) {
        if (index == 0) {
            return point.item;
        }
        return getRecursivePoint(index - 1, point.next);
    }

    public T getRecursive(int index) {
        return getRecursivePoint(index, sentinel.next);
    }

    private class MyIterator implements Iterator<T> {
        Node point = sentinel;

        public boolean hasNext() {
            return point.next != sentinel;
        }

        public T next() {
            if (hasNext()) {
                T value = point.next.item;
                point = point.next;
                return value;
            }
            return null;
        }
    }

    private class Node {
        private T item;
        private Node next;
        private Node previous;

        public Node(T item, Node next, Node previous) {
            this.item = item;
            this.next = next;
            this.previous = previous;
        }
    }

}