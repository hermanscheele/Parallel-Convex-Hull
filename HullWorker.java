
public class HullWorker extends Thread {

    ConvexHull parent;
    int from, to, end, n;
    int x[], y[];
    int t;

    public HullWorker(int from, int to, int n, int x[], int y[], ConvexHull parent, int t) {
        this.from = from;
        this.to = to;
        this.end = to;
        this.n = n;
        this.x = x;
        this.y = y;
        this.parent = parent;
        this.t = t;

    }

    @Override
    public void run() {

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
            if (to == end) {
                try {
                    parent.semaphore.acquire();
                    parent.cHullPoints.add(end);
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    parent.semaphore.release();  // Always release the semaphore
                }
                
            }
            if (to != end) {

                from = to;
                to = end;
                try {
                    parent.semaphore.acquire();
                    parent.cHullPoints.append(sameLine);
                    parent.cHullPoints.add(max_d_point);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    parent.semaphore.release();
                }
            }
        }

        if (max_d < 0) {
            to = max_d_point;
        }

        run();
    }

}
