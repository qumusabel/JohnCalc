package se.itmo.imf.equsolve.math.differential_equation;

import se.itmo.imf.equsolve.BiFunc;

import static java.lang.StrictMath.*;

import java.util.Locale;

/*
f is y' = f(x, y)
y(x, c) is exact solution
c(x, y) is free coefficient
 */
record Equation(BiFunc f, BiFunc y, BiFunc c, String latex, String jessieCode) {
    public double f(double x, double y) {
        return f.apply(x, y);
    }

    public Point[] yInterval(double x0, double xn, double h, double y0) {
        double c_ = c.apply(x0, y0);

        int n = (int) ((xn - x0) / h) + 1;

        Point[] p = new Point[n];
        p[0] = new Point(x0, y0);
        for (int i = 1; i < n; i++) {
            double x = p[i-1].x() + h;
            p[i] = new Point(x, y.apply(x, c_));
        }
        return p;
    }

    public String jessieCode(double x0, double y0) {
        double c_ = c.apply(x0, y0);
        return String.format(Locale.ENGLISH, jessieCode, c_);
    }

    final static Equation[] INSTANCES = new Equation[]{
        new Equation(
            (x, y) -> y + (1 + x) * y * y,
            (x, c) -> -exp(x) / (c + exp(x) * x),
            (x, y) -> -exp(x) / y - exp(x) * x,
            "y^{\\prime} = y + (1 + x) \\cdot y^2",
            "-exp(x) / (%f + exp(x) * x)"
        ),
        new Equation(
            (x, y) -> x * x - 2. * y,
            (x, c) -> c * exp(-2. * x) + x * x / 2. - x / 2. + 1. / 4.,
            (x, y) -> 1. / 4. * exp(2. * x) * (4. * y - 2. * x * x + 2. * x - 1.),
            "y^{\\prime} = x^2 - 2 y",
            "%f * exp(-2. * x) + x * x / 2. - x / 2. + 1. / 4."
        ),
        new Equation(
            (x, y) -> exp(2. * x) + y,
            (x, c) -> c * exp(x) + exp(2. * x),
            (x, y) -> (y - exp(2. * x)) / exp(x),
            "y^{\\prime} = e^{2x} + y",
            "%f * exp(x) + 2 * exp(2 * x)"
        )
    };
}
