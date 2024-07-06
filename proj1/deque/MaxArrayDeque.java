package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    Comparator<T> cc;

    public MaxArrayDeque(Comparator<T> c) {
        cc = c;
    }

    public T max() {
        return max(cc);
    }

    public T max(Comparator<T> c) {
        if (size() == 0) {
            return null;
        }
        T max_value = this.get(0);
        for (int i=0;i<size();i++) {
            if (c.compare(get(i), max_value) > 0) {
                max_value = get(i);
            }
        }
        return max_value;
    }
}
