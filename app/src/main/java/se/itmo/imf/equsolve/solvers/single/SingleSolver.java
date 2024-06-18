package se.itmo.imf.equsolve.solvers.single;

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.signum;

import se.itmo.imf.equsolve.solvers.SolverException;

class SingleSolver {
    private final Equation eqn;
    private final Method method;
    private final double eps;
    private final double a;
    private final double b;

    public SingleSolver(Equation eqn, Method method, double a, double b, double eps) {
        this.eqn = eqn;
        this.method = method;
        this.eps = eps;

        if (a >= b) {
            throw new SolverException("Invalid range");
        }

        if (!hasSingleRootIn(a, b)) {
            throw new SolverException("Zero or many roots found");
        }

        this.a = a;
        this.b = b;
    }

    public Result solve() {
        return switch (method) {
            case SECANT -> solveSecant();
            case BISECTION -> solveBisection();
            case FIXED_POINT -> solveFixedPointIteration();
        };
    }

    private boolean hasSingleRootIn(double a, double b) {
        // Necessary condition
        if (signum(f(a)) == signum(f(b))) {
            // zero, two, ... roots
            return false;
        }

        // Sufficient condition
//        double max = new Minimizer((x) -> -df(x), eps).minimizeIn(a, b).y();
//        double min = new Minimizer((x) -> +df(x), eps).minimizeIn(a, b).y();
//
//        return signum(df(a)) == signum(max) &&
//                signum(max) == signum(min) &&
//                signum(min) == signum(df(b));
        return true;
    }

    private Result solveBisection() {
        double a = this.a, b = this.b;

        double x = (a + b) / 2;
        int iters = 0;
        while (abs(f(x)) > eps && abs(a - b) > eps) {
            iters++;
            x = (a + b) / 2;
            if (signum(f(x)) != signum(f(a))) {
                b = x;
            } else {
                a = x;
            }
        }
        return new Result(x, f(x), iters);
    }

    private Result solveSecant() {
        double a = this.a, b = this.b;

        double xp, x;
        if (signum(f(a)) == signum(ddf(a))) {
            xp = a;
            x = xp + (b - a) * .25;
        } else if (signum(f(b)) == signum(ddf(b))) {
            xp = b;
            x = xp - (b - a) * .25;
        } else {
            xp = (a + b) / 2;
            x = xp + (b - a) * .25;
        }

        int iters = 0;
        while (abs(xp - x) > eps || f(x) > eps) {
            iters++;
            double xn = x - (x - xp) / (f(x) - f(xp)) * f(x);
            xp = x;
            x = xn;
        }
        return new Result(x, f(x), iters);
    }

    private Result solveFixedPointIteration() {
        double df_a = df(a), df_b = df(b);
        double lambda;
        if (df_a > df_b) {
            lambda = abs(1. / df_a);
        } else {
            lambda = abs(1. / df_b);
        }
        if (signum(df_a) != signum(df_b)) {
            throw new SolverException("sign(df(a)) != sign(df(b))... weird");
        } else if (df_a > 0) {
            lambda = -lambda;
        }

        double dphi_a = 1 + lambda * df_a,
                dphi_b = 1 + lambda * df_b;

        if (abs(dphi_a) > 1 || abs(dphi_b) > 1) {
            throw new SolverException("Method does not converge in [a, b]");
        }

        double x;
        if (dphi_a < dphi_b) {
            x = a;
        } else {
            x = b;
        }

        int iters = 0;
        double xn = x;
        do {
            iters++;
            x = xn;
            xn = x + lambda * f(x);
        } while (abs(xn - x) > eps);

        return new Result(x, f(x), iters);
    }

    private double f(double x) {
        return eqn.f().apply(x);
    }

    private double df(double x) {
        return eqn.df().apply(x);
    }

    private double ddf(double x) {
        return eqn.ddf().apply(x);
    }

    public record Result(double x, double y, int iters) {
    }

    public enum Method {
        BISECTION("Bisection method"),
        SECANT("Secant method"),
        FIXED_POINT("Fixed-point iteration");

        private final static String[] names;

        static {
            Method[] values = values();
            names = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                names[i] = values[i].getName();
            }
        }

        private final String name;

        Method(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Method ofName(String name) {
            for (Method m : Method.values()) {
                if (m.name.equals(name)) {
                    return m;
                }
            }
            return null;
//            throw new IllegalArgumentException("Method of name '" + name + "' not found");
        }

        public static String[] getNames() {
            return names;
        }
    }
}
