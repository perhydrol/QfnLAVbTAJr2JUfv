package IntList;

import static org.junit.Assert.*;
import org.junit.Test;

public class SquarePrimesTest {

    /**
     * Here is a test for isPrime method. Try running it.
     * It passes, but the starter code implementation of isPrime
     * is broken. Write your own JUnit Test to try to uncover the bug!
     */
    @Test
    public void testSquarePrimesSimple() {
        IntList lst = IntList.of(14, 15, 16, 17, 18);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("14 -> 15 -> 16 -> 289 -> 18", lst.toString());
        assertTrue(changed);
    }
    @Test
    public void testSquarePrimesSimple2(){
        IntList lst=IntList.of(13,13,15,19,18);
        boolean changed=IntListExercises.squarePrimes(lst);
        assertEquals("169 -> 169 -> 15 -> 361 -> 18",lst.toString());
        assertTrue(changed);
    }

    @Test
    public void testSquarePrimesSimple3(){
        IntList lst=IntList.of(1,1,1,1);
        boolean changed=IntListExercises.squarePrimes(lst);
        assertEquals("1 -> 1 -> 1 -> 1",lst.toString());
        assertFalse(changed);
    }
}
