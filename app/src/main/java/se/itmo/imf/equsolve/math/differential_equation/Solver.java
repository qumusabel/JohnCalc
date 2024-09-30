package se.itmo.imf.equsolve.math.differential_equation;

import java.util.stream.IntStream;
import java.util.Arrays;

class Solver {
    private Solver() {}

    public static Result solve(Equation eqn, Method method, double x0, double xn, double h, double y0, double eps) {
        Point[] result = switch (method) {
            case EULER -> solveEuler(eqn, x0, xn, h, y0);
            case EULER_IMRPOVED -> solveEulerImproved(eqn, x0, xn, h, y0);
            case ADAMS -> solveAdams(eqn, x0, xn, h, y0);
        };

        double resultEps = switch (method) {
            case EULER, EULER_IMRPOVED -> {
                Point[] result2 = switch (method) {
                    case EULER -> solveEuler(eqn, x0, xn, h / 2, y0);
                    case EULER_IMRPOVED -> solveEulerImproved(eqn, x0, xn, h / 2, y0);
                    default -> throw new RuntimeException();
                };

                double pp = (method == Method.EULER) ? 1 : 3;

                double R = StrictMath.abs(result[result.length-1].y() - result2[result2.length-1].y()) / pp;
                yield R;
            }
            case ADAMS -> {
                Point[] exact = eqn.yInterval(x0, xn, h, y0);
                double e = IntStream.range(0, Math.min(exact.length, result.length))
                        .mapToDouble(i -> StrictMath.abs(exact[i].y() - result[i].y()))
                        .max().getAsDouble();
                yield e;
            }
        };

        return new Result(result, resultEps);
    }

    private static Point[] solveEuler(Equation eqn, double x0, double xn, double h, double y0) {
        int n = (int) ((xn - x0) / h) + 1;
        Point[] p = new Point[n];

        p[0] = new Point(x0, y0);
        for (int i = 1; i < n; i++) {
            double y = p[i-1].y() + h * eqn.f(p[i-1].x(), p[i-1].y());
            double x = p[i-1].x() + h;
            p[i] = new Point(x, y);
        }

        return p;
    }

    private static Point[] solveEulerImproved(Equation eqn, double x0, double xn, double h, double y0) {
        int n = (int) ((xn - x0) / h) + 1;
        Point[] p = new Point[n];

        p[0] = new Point(x0, y0);

        for (int i = 1; i < n; i++) {
            double x = p[i-1].x() + h;
            double ya = p[i-1].y() + h * eqn.f(p[i-1].x(), p[i-1].y());
            double y = p[i-1].y() + h / 2 * (eqn.f(p[i-1].x(), p[i-1].y()) + eqn.f(x, ya));
            p[i] = new Point(x, y);
        }

        return p;
    }

    private static Point[] solveAdams(Equation eqn, double x0, double xn, double h, double y0) {
        int n = (int) ((xn - x0) / h) + 1;
        Point[] p = new Point[n];

        p[0] = new Point(x0, y0);

        double[] f = new double[n];
        f[0] = eqn.f(p[0].x(), p[0].y());

        // Obtain 3 points using Euler's method
        for (int i = 1; i <= 3; i++) {
            double y = p[i-1].y() + h * eqn.f(p[i-1].x(), p[i-1].y());
            double x = p[i-1].x() + h;
            p[i] = new Point(x, y);
            f[i] = eqn.f(x, y);
        }

        for (int i = 4; i < n; i++) {
            double df = f[i-1] - f[i-2];
            double d2f = f[i-1] - 2*f[i-2] + f[i-3];
            double d3f = f[i-1] - 3*f[i-2] + 3*f[i-3] - f[i-4];

            double x = p[i-1].x() + h;
            double y = p[i-1].y()
                    + h * f[i-1]
                    + h * h / 2 * df
                    + 5 * h * h * h / 12 * d2f
                    + 3 * h * h * h * h / 8 * d3f;

            p[i] = new Point(x, y);
            f[i] = eqn.f(x, y);
        }

        return p;
    }

    public record Result(Point[] p, double eps) {}

    public enum Method {
        EULER("Euler's"),
        EULER_IMRPOVED("Euler's improved"),
        ADAMS("Adams'");

        final String name;
        Method(String name) {
            this.name = name;
        }

        static String[] names() {
            return Arrays.stream(values()).map(m -> m.name).toArray(String[]::new);
        }
    }
}
