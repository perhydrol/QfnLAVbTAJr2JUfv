package deque;

import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MaxArraydequeTest {
    @Test
    public void equalTest() {
        ArrayDeque<Integer> one = new ArrayDeque<>();
        LinkedListDeque<Integer> two = new LinkedListDeque<>();
        for (int i = 0; i < 100; i++) {
            one.addLast(i);
            two.addLast(i);
        }
        assertTrue(one.equals(two));
        assertTrue(two.equals(one));
    }

    @Test
    public void biggestIntTest() {
        MaxArrayDeque<Integer> lld1 = new MaxArrayDeque<>(new IntComparator());
        for (int i = 0; i < 10; i++) {
            lld1.addLast(i);
        }
        assertTrue(9 == lld1.max());
    }

    @Test
    public void biggestStringTest() {
        MaxArrayDeque<String> lld1 = new MaxArrayDeque<>(new StringComparator());
        for (int i = 65; i < 65 + 26; i++) {
            lld1.addLast(String.valueOf((char) i));
        }
        assertEquals(String.valueOf('Z'), lld1.max());
    }

    public class IntComparator implements Comparator<Integer> {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o1 - o2;
        }
    }

    public class StringComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }
}
