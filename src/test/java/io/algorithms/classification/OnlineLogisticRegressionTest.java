package io.algorithms.classification;
import com.google.common.collect.Lists;
import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnlineLogisticRegressionTest {

    public static class Point {
        public final double x;
        public final double y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object arg0) {
            if (!(arg0 instanceof Point)) {
                return false;
            }
            Point p = (Point) arg0;
            return ((this.x == p.x) && (this.y == p.y));
        }

        @Override
        public int hashCode() {
            long h = Double.doubleToLongBits(x) * 31 + Double.doubleToLongBits(y);
            return (int) (h >>> 32 ^ h);
        }

        @Override
        public String toString() {
            return String.format("Point(%f, %f)", this.x, this.y);
        }
    }

    @Test
    public void testABC() {

        Map<Point, Integer> points = new HashMap<Point, Integer>();

        points.put(new Point(0, 0), 0);
        points.put(new Point(1, 1), 0);
        points.put(new Point(1, 0), 0);
        points.put(new Point(0, 1), 0);
        points.put(new Point(2, 2), 0);

        points.put(new Point(8, 8), 1);
        points.put(new Point(8, 9), 1);
        points.put(new Point(9, 8), 1);
        points.put(new Point(9, 9), 1);

        OnlineLogisticRegression learningAlgo = new OnlineLogisticRegression(2, 3, new L1());

        // this is a really big value whih will make the model very cautious
        // for lambda = 0.1, the first example below should be about .83 certain
        // for lambda = 0.01, the first example below should be about 0.98 certain
        learningAlgo.lambda(0.1);
        learningAlgo.learningRate(4);

        System.out.println("training model  \n");
        final List<Point> keys = Lists.newArrayList(points.keySet());
        // 200 times through the training data is probably over-kill.  It doesn't matter
        // for tiny data.  The key here is total number of points seen, notnumber of passes.
        for (int i = 0; i < 200; i++) {
            // randomize training data on each iteration
            Collections.shuffle(keys);
            for (Point point : keys) {
                Vector v = getVector(point);
                learningAlgo.train(points.get(point), v);
            }
        }
        learningAlgo.close();

//        LogisticRegressionClassifier.printMatrix(learningAlgo);
        
        //now classify real data
        Vector v = new RandomAccessSparseVector(3);
        v.set(0, 0.5);
        v.set(1, 0.5);
        v.set(2, 1);

        Vector r = learningAlgo.classifyFull(v);
        System.out.println(r);

        System.out.println("ans = ");
        System.out.printf("no of categories = %d\n",
learningAlgo.numCategories());
        System.out.printf("no of features = %d\n",
learningAlgo.numFeatures());
        System.out.printf("Probability of cluster 0 = %.3f\n", r.get(0));
        System.out.printf("Probability of cluster 1 = %.3f\n", r.get(1));

        v.set(0, 4.5);
        v.set(1, 6.5);
        v.set(2, 1);

        r = learningAlgo.classifyFull(v);

        System.out.println("ans = ");
        System.out.printf("no of categories = %d\n",
learningAlgo.numCategories());
        System.out.printf("no of features = %d\n",
learningAlgo.numFeatures());
        System.out.printf("Probability of cluster 0 = %.3f\n", r.get(0));
        System.out.printf("Probability of cluster 1 = %.3f\n", r.get(1));

        // show how the score varies along a line from 0,0 to 1,1
        System.out.printf("\nx\tscore\n");
        for (int i = 0; i < 100; i++) {
            final double x = 0.0 + i / 10.0;
            v.set(0, x);
            v.set(1, x);
            v.set(2, 1);

            r = learningAlgo.classifyFull(v);

            System.out.printf("%.2f\t%.3f\n", x, r.get(1));
        }

    }

    private static Vector getVector(Point point) {
        Vector v = new DenseVector(3);
        v.set(0, point.x);
        v.set(1, point.y);
        v.set(2, 1);

        return v;
    }
}