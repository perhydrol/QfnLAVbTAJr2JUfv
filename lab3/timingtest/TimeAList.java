package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeAList {
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
        timeAListConstruction();
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
    public static void timeAListConstruction() {
        // TODO: YOUR CODE HERE
        int COUNT=10;
        AList<Integer> Ns=new AList<Integer>();
        AList<Double> times=new AList<Double>();
        AList<Integer> opCounts=new AList<Integer>();
        Stopwatch sw=new Stopwatch();
        for(int i=0;i<COUNT;i+=1){
            int size=1000*pow(2,i);
            double timeInSeconds=sw.elapsedTime();
            AList<Integer> temp= new AList<Integer>();
            for (int j=0;j<size;j++){
                temp.addLast(j);
            }
            times.addLast(timeInSeconds);
            Ns.addLast(size);
            opCounts.addLast(size);
        }
        printTimingTable(Ns,times,opCounts);
    }
}
