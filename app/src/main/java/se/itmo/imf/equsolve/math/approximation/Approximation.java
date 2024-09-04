package se.itmo.imf.equsolve.math.approximation;

import static java.lang.StrictMath.log;
import static java.lang.StrictMath.exp;
import static java.lang.StrictMath.pow;

import java.util.Arrays;

interface Approximation {
    double[] getParams();
    double at(double x);

    class Linear implements Approximation {
        double[] params;

        Linear(TableFunction func) {
            params = PolynomialApproximator.approximate(func, 1);
        }

        @Override
        public double[] getParams() {
            return Arrays.copyOf(this.params, this.params.length);
        }

        @Override
        public double at(double x) {
            return params[0] + params[1] * x;
        }
    }

    class Quadratic implements Approximation {
        double[] params;

        Quadratic(TableFunction func) {
            params = PolynomialApproximator.approximate(func, 2);
        }

        @Override
        public double[] getParams() {
            return Arrays.copyOf(this.params, this.params.length);
        }

        @Override
        public double at(double x) {
            return params[0] + params[1] * x + params[2] * x * x;
        }
    }

    class Cubic implements Approximation {
        double[] params;

        Cubic(TableFunction func) {
            params = PolynomialApproximator.approximate(func, 3);
        }

        @Override
        public double[] getParams() {
            return Arrays.copyOf(this.params, this.params.length);
        }

        @Override
        public double at(double x) {
            return params[0] + params[1] * x + params[2] * x * x + params[3] * x * x * x;
        }
    }

    class Exponential implements Approximation {
        final double a, b;

        Exponential(TableFunction func) {
            func = new TableFunction(
                    Arrays.stream(func.points()).map(p ->
                                    new TableFunction.Point(p.x(), log(p.y())))
                            .toArray(TableFunction.Point[]::new));
            double[] params = PolynomialApproximator.approximate(func, 1);
            a = exp(params[0]);
            b = params[1];
        }

        @Override
        public double[] getParams() {
            return new double[]{a, b};
        }

        @Override
        public double at(double x) {
            return a * exp(b * x);
        }
    }

    class Logarithmic implements Approximation {
        final double a, b;

        Logarithmic(TableFunction func) {
            func = new TableFunction(
                    Arrays.stream(func.points()).map(p ->
                                    new TableFunction.Point(log(p.x()), p.y()))
                            .toArray(TableFunction.Point[]::new));
            double[] params = PolynomialApproximator.approximate(func, 1);
            a = params[0];
            b = params[1];
        }

        @Override
        public double[] getParams() {
            return new double[]{a, b};
        }

        @Override
        public double at(double x) {
            return a * log(x) + b;
        }
    }

    class Power implements Approximation {
        final double a, b;

        Power(TableFunction func) {
            func = new TableFunction(
                    Arrays.stream(func.points()).map(p ->
                                    new TableFunction.Point(log(p.x()), log(p.y())))
                            .toArray(TableFunction.Point[]::new));
            double[] params = PolynomialApproximator.approximate(func, 1);
            a = exp(params[0]);
            b = params[1];
        }

        @Override
        public double[] getParams() {
            return new double[]{a, b};
        }

        @Override
        public double at(double x) {
            return a * pow(x, b);
        }
    }
}
