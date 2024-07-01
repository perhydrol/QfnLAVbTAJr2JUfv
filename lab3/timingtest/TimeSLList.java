package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }
    private static int pow(int j,int i){
        if(i==0){
            return 1;
        }
        int ans=1;
        for(int ii=0;ii<i;ii++){
            ans*=j;
        }
        return ans;
    }
    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        int COUNT=6;
        AList<Integer> Ns=new AList<Integer>();
        AList<Double> times=new AList<Double>();
        AList<Integer> opCounts=new AList<Integer>();
        Stopwatch sw=new Stopwatch();
        for(int i=0;i<COUNT;i+=1){
            int size=1000*pow(2,i);
            SLList<Integer> temp= new SLList<Integer>();
            for (int j=0;j<size;j++){
                temp.addLast(j);
            }
            double timeInSeconds=sw.elapsedTime();
            temp.getLast();
            times.addLast(timeInSeconds);
            Ns.addLast(size);
            opCounts.addLast(size);
        }
        printTimingTable(Ns,times,opCounts);
    }
}
