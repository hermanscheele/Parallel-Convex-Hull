import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Semaphore;

public class ConvexHull {
    int n, seed, MAX_X, MAX_Y, MIN_X, MIN_Y;
    int x[], y[];
    public IntList correctOrder;
    public IntList cHullPoints;
    public Semaphore semaphore;
    public long startSeq;
    public long endSeq;
    public long startPar;
    public long endPar;

    ConvexHull(final int n, final int seed, final NPunkter17 nPunkter17) {
        this.n = n;
        this.seed = seed;
        this.x = new int[n];
        this.y = new int[n];
        this.correctOrder = new IntList();
        this.cHullPoints = new IntList();
        nPunkter17.fyllArrayer(x, y);
    }

    public void startSeq() {
        // Finding first line
        for (int i = 0; i < n; i++) {
            if (x[i] > x[MAX_X])
                MAX_X = i;
            else if (x[i] < x[MIN_X])
                MIN_X = i;
            if (y[i] > y[MAX_Y])
                MAX_Y = i;

        }
        correctOrder.add(MAX_X);

        // find line (ax + by + c = 0)
        int a = y[MAX_X] - y[MIN_X];
        int b = x[MIN_X] - x[MAX_X];
        int c = (y[MIN_X] * x[MAX_X]) - (y[MAX_X] * x[MIN_X]);

        // find point with largest d
        int max_d = a * x[0] + b * y[0] + c;
        int max_d_point = 0;

        for (int i = 0; i < n; i++) {
            int d = a * x[i] + b * y[i] + c;
            if (d < max_d) {
                max_d_point = i;
                max_d = d;
            }
        }

        startSeq = System.nanoTime();
        recursive(MAX_X, max_d_point, true);

    }

    public void recursive(int from, int to, boolean first) {

        if (!first) {

            // finds new line (pn -> pn+1)
            int a = y[from] - y[to];
            int b = x[to] - x[from];
            int c = (y[to] * x[from]) - (y[from] * x[to]);

            // find point with largest d
            int max_d = 0;
            int max_d_point = to;
            IntList sameLine = new IntList();

            for (int i = 0; i < n; i++) {
                int d = a * x[i] + b * y[i] + c;
                if (d < max_d) {
                    max_d_point = i;
                    max_d = d;
                }
                if (d == 0 && i != to && i != from) {
                    sameLine.add(i);
                }
            }

            if (max_d_point == to) {
                if (to == MAX_X) {
                    correctOrder.add(MAX_X);
                    endSeq = System.nanoTime();
                    return;
                }
                from = to;
                to = MAX_X;
                correctOrder.append(sameLine);
                correctOrder.add(max_d_point);
            }

            if (max_d < 0) {
                to = max_d_point;
            }
        }

        if (first) {

            // finds new line (pn -> pn+1)
            int a = y[from] - y[to];
            int b = x[to] - x[from];
            int c = (y[to] * x[from]) - (y[from] * x[to]);

            // find point with largest d
            int max_d = 0;
            int max_d_point = to;
            IntList sameLine = new IntList();

            for (int i = 0; i < n; i++) {
                int d = a * x[i] + b * y[i] + c;
                if (d < max_d) {
                    max_d_point = i;
                    max_d = d;
                }
                if (d == 0 && i != to && i != from) {
                    sameLine.add(i);
                }
            }

            if (max_d_point == to) {
                if (to == MIN_X) {
                    correctOrder.add(MIN_X);
                    first = false;
                }
                if (to != MIN_X) {
                    from = to;
                    to = MIN_X;
                    correctOrder.append(sameLine);
                    correctOrder.add(max_d_point);
                }
            }

            if (max_d < 0) {
                to = max_d_point;
            }
        }

        recursive(from, to, first);

    }

    public void startPar(int numOfThreads) {
        this.semaphore = new Semaphore(numOfThreads);

        // [MAX_X, MIN_X, MAX_Y, MIN_Y]
        ArrayList<Integer> linePoints = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if (x[i] > x[MAX_X]) {
                MAX_X = i;
            } else if (x[i] < x[MIN_X])
                MIN_X = i;

            if (y[i] > y[MAX_Y]) {
                MAX_Y = i;
            } else if (y[i] < y[MIN_Y])
                MIN_Y = i;
        }

        linePoints.add(MAX_X);
        linePoints.add(MIN_X);
        linePoints.add(MAX_Y);
        linePoints.add(MIN_Y);

        Thread[] threads = new HullWorker[numOfThreads];
        if (numOfThreads == 2) {
            Thread thread1 = new HullWorker(linePoints.get(0), linePoints.get(1), n, x, y, this, 1);
            Thread thread2 = new HullWorker(linePoints.get(1), linePoints.get(0), n, x, y, this, 2);
            threads[0] = thread1;
            threads[1] = thread2;
        }

        else if (numOfThreads == 4) {
            Thread thread1 = new HullWorker(linePoints.get(0), linePoints.get(2), n, x, y, this, 1);
            Thread thread2 = new HullWorker(linePoints.get(2), linePoints.get(1), n, x, y, this, 2);
            Thread thread3 = new HullWorker(linePoints.get(1), linePoints.get(3), n, x, y, this, 3);
            Thread thread4 = new HullWorker(linePoints.get(3), linePoints.get(0), n, x, y, this, 4);
            threads[0] = thread1;
            threads[1] = thread2;
            threads[2] = thread3;
            threads[3] = thread4;
        }

        else {
            System.out.println("parallel program is restricted to having 2 or 4 threads");
        }

        startPar = System.nanoTime();
        for (Thread w : threads) {
            w.start();
        }

        try {
            for (Thread w : threads) {
                w.join();
            }
        } catch (InterruptedException e) {
            System.out.println("thread was interrupted.");
        }

        parCorrectOrder(MAX_X, MIN_X, true);
    }

    public void parCorrectOrder(int from, int to, boolean first) {

        if (!first) {

            // finds new line (pn -> pn+1)
            int a = y[from] - y[to];
            int b = x[to] - x[from];
            int c = (y[to] * x[from]) - (y[from] * x[to]);

            // find point with largest d
            int max_d = 0;
            int max_d_point = to;
            IntList sameLine = new IntList();

            for (int i = 0; i < cHullPoints.size(); i++) {
                int d = a * x[cHullPoints.get(i)] + b * y[cHullPoints.get(i)] + c;
                if (d < max_d) {
                    max_d_point = cHullPoints.get(i);
                    max_d = d;
                }
                if (d == 0 && cHullPoints.get(i) != to && cHullPoints.get(i) != from) {
                    sameLine.add(cHullPoints.get(i));
                }
            }

            if (max_d_point == to) {
                if (to == MAX_X) {
                    correctOrder.add(MAX_X);
                    endPar = System.nanoTime();
                    return;
                }
                from = to;
                to = MAX_X;
                correctOrder.append(sameLine);
                correctOrder.add(max_d_point);
            }

            if (max_d < 0) {
                to = max_d_point;
            }
        }

        if (first) {

            // finds new line (pn -> pn+1)
            int a = y[from] - y[to];
            int b = x[to] - x[from];
            int c = (y[to] * x[from]) - (y[from] * x[to]);

            // find point with largest d
            int max_d = 0;
            int max_d_point = to;
            IntList sameLine = new IntList();

            for (int i = 0; i < cHullPoints.size(); i++) {
                int d = a * x[cHullPoints.get(i)] + b * y[cHullPoints.get(i)] + c;
                if (d < max_d) {
                    max_d_point = cHullPoints.get(i);
                    max_d = d;
                }
                if (d == 0 && cHullPoints.get(i) != to && cHullPoints.get(i) != from) {
                    sameLine.add(cHullPoints.get(i));
                }
            }

            if (max_d_point == to) {
                if (to == MIN_X) {
                    correctOrder.add(MIN_X);
                    first = false;
                }
                if (to != MIN_X) {
                    from = to;
                    to = MIN_X;
                    correctOrder.append(sameLine);
                    correctOrder.add(max_d_point);
                }
            }

            if (max_d < 0) {
                to = max_d_point;
            }
        }

        parCorrectOrder(from, to, first);
    }

    public IntList getCorrectOrder() {
        return this.correctOrder;
    }

    public void getChullPoints() {
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("point on the cHull:");
        for (int i = 0; i < cHullPoints.size(); i++) {
            System.out.println(cHullPoints.get(i));
        }
    }

    public static void main(String[] args) {

        
        int n = 100;
        int seed = 0;

        if (args.length > 0){
            try {
                n = Integer.parseInt(args[0]);
                seed = Integer.parseInt(args[1]);
            }
            catch (NumberFormatException e){
                System.out.println("error: argument is not valid integer");
            }
        }
        else {
            System.out.println("please provide an n and seed");
        }

        // number of threads in the parallel version
        int numOfThreads = 2;

        System.out.println("");
        System.out.println("");
        System.out.println("CONVEX HULL");
        System.out.println("n = " + n);
        System.out.println("seed = " + seed);


        System.out.println("");
        System.out.println("");

        ArrayList<Double> seqTimes = new ArrayList<>();
        ArrayList<Double> parTimes = new ArrayList<>();


        // objects for drawing thr graph
        ConvexHull cHullGraph = null;
        IntList correctOrderGraph = null;

        // 7 takes for timing
        for (int i = 0; i < 7; i++) {

            ConvexHull ch = new ConvexHull(n, seed, new NPunkter17(n, seed));

            // sequential
            ch.startSeq();
            seqTimes.add((ch.endSeq - ch.startSeq) / 1000000.0);

            // parallel
            ch.correctOrder = new IntList();
            ch.startPar(numOfThreads);
            parTimes.add((ch.endPar - ch.startPar) / 1000000.0);

            cHullGraph = ch;
            correctOrderGraph = ch.getCorrectOrder();
        }

        Collections.sort(seqTimes);
        Collections.sort(parTimes);

        System.out.println("median timing:");
        System.out.println("seq: " + seqTimes.get(3) + " ms");
        System.out.println("par: " + parTimes.get(3) + " ms");

        

        // drawing graph last graph
        Oblig4Precode precode = new Oblig4Precode(cHullGraph, correctOrderGraph);

        if (n < 10000)
            precode.writeHullPoints();

        precode.drawGraph();
    }

}