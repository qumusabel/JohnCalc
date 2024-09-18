package se.itmo.imf.equsolve.math.approximation;

import static java.lang.StrictMath.pow;

import java.util.Arrays;
import java.util.stream.IntStream;

import se.itmo.imf.gauss.Gauss;
import se.itmo.imf.gauss.Matrix;
import se.itmo.imf.gauss.Vector;

class PolynomialApproximator {
    static double[] approximate(TableFunction func, int polyDegree) {
        if (polyDegree < 1) {
            throw new IllegalArgumentException("polyDegree must be 1 or more");
        }

        double[] freeColumn = IntStream.rangeClosed(0, polyDegree)
                .mapToDouble(p ->
                        Arrays.stream(func.points())
                                .mapToDouble(point -> pow(point.x(), p) * point.y())
                                .sum()
                ).toArray();

        double[] sums = IntStream.rangeClosed(0, polyDegree * 2)
                .mapToDouble(p ->
                        Arrays.stream(func.points())
                                .mapToDouble(point -> pow(point.x(), p))
                                .sum()
                ).toArray();
        sums[0] = func.numPoints();

        Matrix a = new Matrix(
                IntStream.rangeClosed(0, polyDegree)
                        .mapToObj(i -> new Vector(Arrays.copyOfRange(sums, i, i + polyDegree + 1)))
                        .toArray(Vector[]::new)
        );
        Vector b = new Vector(freeColumn);
        Vector solution = Gauss.solve(a, b);

        return solution.asArray();
    }
}


