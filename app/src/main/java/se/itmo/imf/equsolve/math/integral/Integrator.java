package se.itmo.imf.equsolve.math.integral;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

class Integrator {

    static Result integrate(Function f, double a, double b, double eps, Method method) {
        // Runge denominator = 2^k - 1
        final double kRunge = switch (method) {
            case LEFT_RECTANGLES, MIDDLE_RECTANGLES, RIGHT_RECTANGLES, TRAPEZOIDS -> 3.; // k = 2
            case SIMPSONS -> 15; // k = 4
        };

        int n = 4;
        double prevValue = Double.NaN;

        while (true) {
            double value = switch (method) {
                case LEFT_RECTANGLES -> integrateRectangles(f, a, b, n, Rectangle.LEFT);
                case MIDDLE_RECTANGLES -> integrateRectangles(f, a, b, n, Rectangle.MIDDLE);
                case RIGHT_RECTANGLES -> integrateRectangles(f, a, b, n, Rectangle.RIGHT);
                case TRAPEZOIDS -> integrateTrapezoids(f, a, b, n);
                case SIMPSONS -> integrateSimpsons(f, a, b, n);
            };

            if ((prevValue - value) / kRunge < eps) {
                return new Result(value, n);
            }

            prevValue = value;
            n *= 2;
        }
    }

    private static double integrateRectangles(Function f, double a, double b, int n, Rectangle rect) {
        final double h = (b - a) / n;
        final double bias = h * rect.bias;
        return h * IntStream.range(0, n)
                .mapToDouble(i -> a + i * h + bias)
                .map(x -> f.f(x))
                .sum();
    }

    private static double integrateTrapezoids(Function f, double a, double b, int n) {
        final double h = (b - a) / n;

        final double y0 = f.f(a);
        final double ySum= IntStream.rangeClosed(1, n-1)
                .mapToDouble(i -> a + i * h)
                .map(x -> f.f(x))
                .sum();
        final double yn = f.f(b);

        return h * ((y0 + yn) / 2. + ySum);
    }

    private static double integrateSimpsons(Function f, double a, double b, int n) {
        final double h = (b - a) / n;

        final double y0 = f.f(a);
        final double ySumOdd = IntStream.rangeClosed(1, n - 1)
                .filter(i -> i % 2 == 1)
                .mapToDouble(i -> a + i * h)
                .map(x -> f.f(x))
                .sum();
        final double ySumEven = IntStream.rangeClosed(2, n - 2)
                .filter(i -> i % 2 == 0)
                .mapToDouble(i -> a + i * h)
                .map(x -> f.f(x))
                .sum();
        final double yn = f.f(b);

        return h / 3. * (y0 + 4. * ySumOdd + 2. * ySumEven + yn);
    }

    enum Method {
        LEFT_RECTANGLES("Left rectangles"),
        MIDDLE_RECTANGLES("Middle rectangles"),
        RIGHT_RECTANGLES("Right rectangles"),
        TRAPEZOIDS("Trapezoids"),
        SIMPSONS("Simpson's")
        ;

        public String name;
        Method(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }

    private enum Rectangle {
        LEFT(0), MIDDLE(0.5), RIGHT(1);
        public double bias;
        Rectangle(double bias) {
            this.bias = bias;
        }
    }

    record Result(double value, int n) {}
}
