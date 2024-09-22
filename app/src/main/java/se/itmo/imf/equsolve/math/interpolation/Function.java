package se.itmo.imf.equsolve.math.interpolation;

import static java.lang.StrictMath.*;

record Function(java.util.function.Function<Double, Double> f, String expr) {
    public double f(double x) {
        return f.apply(x);
    }

    public TableFunction evalToTable(double a, double b, int n) {
        double h = (b - a) / (n - 1);
        TableFunction.Point[] points = new TableFunction.Point[n];
        for (int i = 0; i < n; i++) {
            points[i] = new TableFunction.Point(a + i * h, f(a + i * h));
        }
        return new TableFunction(points);
    }

    public static Function[] INSTANCES = new Function[]{
        new Function(x -> x * x, "f = x^2"),
        new Function(x -> sqrt(x), "f = sqrt(x)"),
        new Function(x -> 1 + 0.2 * x - 0.4 * x * x, "f = 1 + 0,2x - 0,4x^2"),
        new Function(x -> sin(2*cos(2*x)), "f = sin (2 cos 2x)")
    };
}
