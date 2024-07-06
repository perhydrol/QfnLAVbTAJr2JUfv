package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Iterable<T>,Deque<T> {
    private T[] item;
    private int size;
    private int length;
    private int begin;
    private int end;

    public ArrayDeque() {
        item = (T[]) new Object[8];
        size = 0;
        begin = 3;
        end = 4;
        length = 8;
    }

    public Iterator<T> iterator() {
        return new MyIterator();
    }

    public boolean equals(Object o) {
        if (!(o instanceof ArrayDeque)) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (!get(i).equals(((ArrayDeque<?>) o).get(i))) {
                return false;
            }
        }
        return true;
    }

    private int index(int x) {
        int temp = x + begin + 1;
        if (temp >= length) {
            temp = x - (length - begin) + 1;
        }
        if (temp < 0 || temp > length) {
            return -1;
        }
        return temp;
    }

    private int go_ahead(int point) {
        if ((point - 1) >= 0) {
            return point - 1;
        } else {
            return length - 1;
        }
    }

    private int go_back(int point) {
        if ((point + 1) < length) {
            return point + 1;
        } else {
            return 0;
        }
    }

    private void resize(int capacity) {
        T[] temp = (T[]) new Object[capacity];
        int point = begin;
        for (int i = 0; i < size; i++) {
            point = go_back(point);
            temp[i] = item[point];
        }
        item = temp;
        length = capacity;
        begin = length - 1;
        end = size;
    }

    @Override
    public void addFirst(T value) {
        if ((size + 1) >= length || go_ahead(begin) == end) {
            resize((size + 1) * 2);
        }
        item[begin] = value;
        begin = go_ahead(begin);
        size++;
    }

    @Override
    public void addLast(T value) {
        if ((size + 1) >= length || go_back(end) == begin) {
            resize((size + 1) * 2);
        }
        item[end] = value;
        end = go_back(end);
        size++;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(get(i) + " ");
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (size != 0 && (double) size / length < 0.25) {
            resize(size * 2);
        }
        int first_index = go_back(begin);
        T first_value = item[first_index];
        if (first_value == null) {
            return null;
        } else {
            item[first_index] = null;
            begin = first_index;
            size--;
            return first_value;
        }
    }

    @Override
    public T removeLast() {
        if (size != 0 && (double) size / length < 0.25) {
            resize(size * 2);
        }
        end = go_ahead(end);
        T last_value = item[end];
        if (last_value == null) {
            return null;
        } else {
            item[end] = null;
            size--;
            return last_value;
        }
    }

    @Override
    public T get(int point) {
        point = index(point);
        return (point == -1) ? null : item[point];
    }

    private class MyIterator implements Iterator<T> {
        int point = 0;

        @Override
        public boolean hasNext() {
            return point < size;
        }

        @Override
        public T next() {
            if (hasNext()) {
                T value = get(point);
                point++;
                return value;
            }
            return null;
        }
    }
}
