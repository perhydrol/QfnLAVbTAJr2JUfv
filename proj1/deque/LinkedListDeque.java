package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
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

    public boolean equals(Object o) {
        if (!(o instanceof Deque) || ((Deque<?>) o).size() != size) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (!get(i).equals(((Deque<?>) o).get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void addFirst(T item) {
        Node newNode = new Node(item, sentinel.next, sentinel);
        Node oldFirst = sentinel.next;
        sentinel.next = newNode;
        oldFirst.previous = newNode;
        size++;
    }

    @Override
    public void addLast(T item) {
        Node newNode = new Node(item, sentinel, sentinel.previous);
        Node oldLast = sentinel.previous;
        sentinel.previous = newNode;
        oldLast.next = newNode;
        size++;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        Node current = sentinel.next;
        while (current != sentinel) {
            System.out.print(current.item + " ");
            current = current.next;
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        Node oldFirst = sentinel.next;
        Node newFirst = oldFirst.next;
        sentinel.next = newFirst;
        newFirst.previous = sentinel;
        size--;
        return oldFirst.item;
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        Node oldLast = sentinel.previous;
        Node newLast = oldLast.previous;
        sentinel.previous = newLast;
        newLast.next = sentinel;
        size--;
        return oldLast.item;
    }

    @Override
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

        @Override
        public boolean hasNext() {
            return point.next != sentinel;
        }

        @Override
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

        Node(T item, Node next, Node previous) {
            this.item = item;
            this.next = next;
            this.previous = previous;
        }
    }
}
