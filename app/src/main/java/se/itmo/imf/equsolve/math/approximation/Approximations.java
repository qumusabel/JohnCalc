package se.itmo.imf.equsolve.math.approximation;

import static java.lang.StrictMath.exp;
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;
import static java.lang.StrictMath.sqrt;

import java.util.Arrays;
import java.util.Locale;

class Approximations {
    private Approximations() {}

    static class Linear extends Approximation {
        double[] params;
        private Double score;

        Linear(TableFunction func) {
            super(func);
            params = PolynomialApproximator.approximate(this.func, 1);
        }

        @Override
        public double[] getParams() {
            return Arrays.copyOf(this.params, this.params.length);
        }

        @Override
        public double at(double x) {
            return params[0] + params[1] * x;
        }

        // for a Linear approximation, we need to use Pearson's r.
        @Override
        public double getScore() {
            if (score != null) {
                return score;
            }

            Point[] points = getApproximatedPoints();

            double xAvg = Arrays.stream(points).mapToDouble(Point::x).sum() / points.length;
            double yAvg = Arrays.stream(points).mapToDouble(Point::y).sum() / points.length;

            double numerator = Arrays.stream(points).mapToDouble(p -> (p.x() - xAvg) * (p.y() - yAvg)).sum();

            double sumDx2 = Arrays.stream(points).mapToDouble(p -> pow(p.x() - xAvg, 2)).sum();
            double sumDy2 = Arrays.stream(points).mapToDouble(p -> pow(p.y() - yAvg, 2)).sum();
            double denominator = sqrt(sumDx2 * sumDy2);

            score = numerator / denominator;
            return score;
        }

        @Override
        public String describe() {
            return "Linear ax + b\n" +
                    "\ta = " + params[1] + "\n" +
                    "\tb = " + params[0] + "\n" +
                    "\tσ = " + getStdDev() + "\n" +
                    "\tr = " + getScore();
        }

        @Override
        public String getJessieCode() {
            return String.format(Locale.ENGLISH, "%f * x + %f", params[1], params[0]);
        }
    }

    static class Quadratic extends Approximation {
        double[] params;

        Quadratic(TableFunction func) {
            super(func);
            params = PolynomialApproximator.approximate(this.func, 2);
        }

        @Override
        public double[] getParams() {
            return Arrays.copyOf(this.params, this.params.length);
        }

        @Override
        public double at(double x) {
            return params[0] + params[1] * x + params[2] * x * x;
        }

        @Override
        public String describe() {
            return "Quadratic ax² + bx + c\n" +
                    "\ta = " + params[2] + "\n" +
                    "\tb = " + params[1] + "\n" +
                    "\tc = " + params[0] + "\n" +
                    "\tσ = " + getStdDev() + "\n" +
                    "\tR² = " + getScore();
        }

        @Override
        public String getJessieCode() {
            return String.format(Locale.ENGLISH, "%f * x^2 + %f * x + %f", params[2], params[1], params[0]);
        }
    }

    static class Cubic extends Approximation {
        double[] params;

        Cubic(TableFunction func) {
            super(func);
            params = PolynomialApproximator.approximate(this.func, 3);
        }

        @Override
        public double[] getParams() {
            return Arrays.copyOf(this.params, this.params.length);
        }

        @Override
        public double at(double x) {
            return params[0] + params[1] * x + params[2] * x * x + params[3] * x * x * x;
        }

        @Override
        public String describe() {
            return "Cubic ax³ + bx² + cx + d\n" +
                    "\ta = " + params[3] + "\n" +
                    "\tb = " + params[2] + "\n" +
                    "\tc = " + params[1] + "\n" +
                    "\td = " + params[0] + "\n" +
                    "\tσ = " + getStdDev() + "\n" +
                    "\tR² = " + getScore();
        }

        @Override
        public String getJessieCode() {
            return String.format(Locale.ENGLISH, "%f * x^3 + %f * x^2 + %f * x + %f", params[3], params[2], params[1], params[0]);
        }
    }

    static class Exponential extends Approximation {
        final double a, b;

        Exponential(TableFunction func) {
            super(func);
            TableFunction linFunc = new TableFunction(
                    Arrays.stream(func.points()).map(p ->
                                    new TableFunction.Point(p.x(), log(p.y())))
                            .toArray(TableFunction.Point[]::new));
            double[] params = PolynomialApproximator.approximate(linFunc, 1);
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

        @Override
        public String describe() {
            return "Exponential a⋅exp(bx)\n" +
                    "\ta = " + a + "\n" +
                    "\tb = " + b + "\n" +
                    "\tσ = " + getStdDev() + "\n" +
                    "\tR² = " + getScore();
        }

        @Override
        public String getJessieCode() {
            return String.format(Locale.ENGLISH, "%f * exp(%f * x)", a, b);
        }
    }

    static class Logarithmic extends Approximation {
        final double a, b;

        Logarithmic(TableFunction func) {
            super(func);
            TableFunction linFunc = new TableFunction(
                    Arrays.stream(func.points()).map(p ->
                                    new TableFunction.Point(log(p.x()), p.y()))
                            .toArray(TableFunction.Point[]::new));
            double[] params = PolynomialApproximator.approximate(linFunc, 1);
            a = params[1];
            b = params[0];
        }

        @Override
        public double[] getParams() {
            return new double[]{a, b};
        }

        @Override
        public double at(double x) {
            return a * log(x) + b;
        }

        @Override
        public String describe() {
            return "Logarithmic a⋅log(x) + b\n" +
                    "\ta = " + a + "\n" +
                    "\tb = " + b + "\n" +
                    "\tσ = " + getStdDev() + "\n" +
                    "\tR² = " + getScore();
        }

        @Override
        public String getJessieCode() {
            return String.format(Locale.ENGLISH, "%f * log(x) + %f", a, b);
        }
    }

    static class Power extends Approximation {
        final double a, b;

        Power(TableFunction func) {
            super(func);
            TableFunction linFunc = new TableFunction(
                    Arrays.stream(func.points()).map(p ->
                                    new TableFunction.Point(log(p.x()), log(p.y())))
                            .toArray(TableFunction.Point[]::new));
            double[] params = PolynomialApproximator.approximate(linFunc, 1);
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

        @Override
        public String describe() {
            return "Power a⋅x^b\n" +
                    "\ta = " + a + "\n" +
                    "\tb = " + b + "\n" +
                    "\tσ = " + getStdDev() + "\n" +
                    "\tR² = " + getScore();
        }

        @Override
        public String getJessieCode() {
            return String.format(Locale.ENGLISH, "%f * pow(x, %f)", a, b);
        }
    }
}
