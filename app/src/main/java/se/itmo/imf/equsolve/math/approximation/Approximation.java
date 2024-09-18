package se.itmo.imf.equsolve.math.approximation;

import static java.lang.StrictMath.pow;
import static java.lang.StrictMath.sqrt;

import java.util.Arrays;

abstract class Approximation {
    protected TableFunction func;
    private Double stdDev;
    private Double score;

    protected Approximation(TableFunction func) {
        this.func = func;
    }

    public abstract double at(double x);
    public abstract double[] getParams();
    public abstract String describe();
    public abstract String getJessieCode();

    public final double getStdDev() {
        if (stdDev != null) {
            return stdDev;
        }

        Point[] points = getApproximatedPoints();

        double epsSquareSum = Arrays.stream(points).mapToDouble(p -> pow(p.eps, 2)).sum();
        stdDev = sqrt(epsSquareSum / points.length);
        return stdDev;
    }

    // By default, we compute R^2
    public double getScore() {
        if (score != null) {
            return score;
        }

        Point[] points = getApproximatedPoints();

        double numerator = Arrays.stream(points).mapToDouble(p -> pow(p.eps, 2)).sum();
        double yApproxAvg = Arrays.stream(points).mapToDouble(p -> p.y_).sum() / points.length;
        double denominator = Arrays.stream(points).mapToDouble(p -> pow(p.y - yApproxAvg, 2)).sum();
        score = 1 - numerator / denominator;
        return score;
    }

    public final Point[] getApproximatedPoints() {
        Point[] points = Arrays.stream(func.points()).map(p -> {
            double y_ = at(p.x());
            return new Point(p.x(), p.y(), y_, y_ - p.y());
        }).toArray(Point[]::new);
        return points;
    }

    public record Point(double x, double y, double y_, double eps) {}
}
