package se.itmo.imf.equsolve.math.approximation;

import java.util.Arrays;
import java.util.List;

import static java.lang.StrictMath.pow;
import static java.lang.StrictMath.sqrt;

class Approximator {
    private final TableFunction func;
    private final List<Approximation> toCompute;

    public Approximator(TableFunction func) {
        this.func = func;
        toCompute = List.of(
            new Approximation.Linear(func),
            new Approximation.Quadratic(func),
            new Approximation.Cubic(func),
            new Approximation.Exponential(func),
            new Approximation.Logarithmic(func),
            new Approximation.Power(func)
        );
    }

    public Result findBestApproximation() {
        double minStdDev = Double.MAX_VALUE;
        Result bestResult = null;

        for (Approximation approximation : toCompute) {
            Point[] points = Arrays.stream(func.points()).map(p -> {
                double yApprox = approximation.at(p.x());
                return new Point(p.x(), p.y(), yApprox, yApprox - p.y());
            }).toArray(Point[]::new);

            double stdDev = Arrays.stream(points).mapToDouble(p -> pow(p.eps, 2)).sum() / points.length;
            if (stdDev < minStdDev) {
                minStdDev = stdDev;

                double score;
                if (approximation instanceof Approximation.Linear) {
                    score = rPearson(points);
                } else {
                    score = rSquared(points);
                }

                bestResult = new Result(approximation, points, stdDev, score);
            }
        }

        return bestResult;
    }

    private static double rPearson(Point[] points) {
        double xAvg = Arrays.stream(points).mapToDouble(p -> p.x).sum() / points.length;
        double yAvg = Arrays.stream(points).mapToDouble(p -> p.y).sum() / points.length;

        double numerator = Arrays.stream(points).mapToDouble(p -> (p.x - xAvg) * (p.y - yAvg)).sum();

        double sumDx2 = Arrays.stream(points).mapToDouble(p -> pow(p.x - xAvg, 2)).sum();
        double sumDy2 = Arrays.stream(points).mapToDouble(p -> pow(p.y - yAvg, 2)).sum();
        double denominator = sqrt(sumDx2 * sumDy2);

        return numerator / denominator;
    }

    private static double rSquared(Point[] points) {
        double numerator = Arrays.stream(points).mapToDouble(p -> pow(p.y - p.yApprox, 2)).sum();
        double yApproxAvg = Arrays.stream(points).mapToDouble(p -> p.yApprox).sum() / points.length;
        double denominator = Arrays.stream(points).mapToDouble(p -> pow(p.y - yApproxAvg, 2)).sum();
        return 1 - numerator / denominator;
    }

    public record Point(double x, double y, double yApprox, double eps) {}
    public record Result(Approximation approximation, Point[] points, double stdDev, double score) {}
}
