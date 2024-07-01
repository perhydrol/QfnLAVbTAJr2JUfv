package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    private static boolean isEq(AListNoResizing<Integer> x,BuggyAList<Integer> y){
        int x_size=x.size();
        int y_size=y.size();
        if(x_size==y_size){
            for(int i=0;i<x_size;i++){
                if(x.get(i)!=y.get(i)){
                    return false;
                }
            }
        }else {
            return false;
        }
        return true;
    }
  // YOUR TESTS HERE
    @Test
    public void randomizedTest(){
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> B=new BuggyAList<>();
        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                B.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int l_size = L.size();
                int b_size = B.size();
                assertEquals(l_size,b_size);
            }else if(L.size()>0&&B.size()>0&&operationNumber==2){
                int l_last=L.getLast();
                int b_last =B.getLast();
                assertEquals(l_last,b_last);
            }else if(L.size()>0&&B.size()>0&&operationNumber==3){
                int l_remove =L.removeLast();
                int b_remove =B.removeLast();
                assertEquals(l_remove,b_remove);
            }
        }

    }

    @Test
    public void testThreeAddTreeRemove(){
        AListNoResizing<Integer> list_no_resizing=new AListNoResizing<>();
        BuggyAList<Integer> list_bug=new BuggyAList<>();

        list_no_resizing.addLast(4);
        list_bug.addLast(4);

        list_no_resizing.addLast(5);
        list_bug.addLast(5);

        list_no_resizing.addLast(6);
        list_bug.addLast(6);
        assertTrue(isEq(list_no_resizing,list_bug));

        list_no_resizing.removeLast();
        list_bug.removeLast();
        assertTrue(isEq(list_no_resizing,list_bug));

        list_no_resizing.removeLast();
        list_bug.removeLast();
        assertTrue(isEq(list_no_resizing,list_bug));
    }
}
