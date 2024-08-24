package se.itmo.imf.equsolve.math.two_equations;

import static java.lang.StrictMath.*;

import se.itmo.imf.equsolve.math.SolverException;

class DualSystemSolver {
    private final double x;
    private final double y;
    private final double r;
    private final double eps;
    private final EquationSystem eqs;
    private final BiEquation eq1, eq2;

    DualSystemSolver(double x, double y, double r, double eps, EquationSystem eqs) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.eps = eps;
        this.eqs = eqs;
        this.eq1 = eqs.eq1();
        this.eq2 = eqs.eq2();
    }

    private Double lambdax, lambday;

    public Result solve() {
        {
            double[] df1Bounds = new double[]{
                eq1.dfdx(x - r, y - r),
                eq1.dfdx(x + r, y - r),
                eq1.dfdx(x - r, y + r),
                eq1.dfdx(x + r, y + r)
            };
            int maxIndex = maxIndex(df1Bounds);
            lambdax = -1 / df1Bounds[maxIndex];
        }
        {
            double[] df2Bounds = new double[]{
                eq2.dfdy(x - r, y - r),
                eq2.dfdy(x + r, y - r),
                eq2.dfdy(x - r, y + r),
                eq2.dfdy(x + r, y + r)
            };
            int maxIndex = maxIndex(df2Bounds);
            lambday = -1 / df2Bounds[maxIndex];
        }
        {
            double[] dphiBounds = new double[]{
                dphi(x - r, y - r),
                dphi(x + r, y - r),
                dphi(x - r, y + r),
                dphi(x + r, y - r)
            };
            int maxIndex = maxIndex(dphiBounds);
            if (abs(dphiBounds[maxIndex]) > 1) {
                throw new SolverException("Simple iterations do not converge");
            }
        }

        double x = this.x, y = this.y;
        double xn = x, yn = y;
        int iters = 0;
        do {
            iters++;
            x = xn;
            y = yn;
            xn = x + lambdax * eq1.f(x, y);
            yn = y + lambday * eq2.f(x, y);
        } while (abs(x - xn) > eps && abs(y - yn) > eps);

        return new Result(xn, yn, abs(x - xn), abs(y - yn), iters);
    }

    private double dphi(double x, double y) {
        // phi1 = x + lambdax * eq1.f(x, y)
        // phi2 = y + lambday * eq2.f(x, y)
        double
                dphi1dx = 1 + lambdax * eq1.dfdx(x, y),
                dphi1dy = lambdax * eq1.dfdy(x, y),
                dphi2dx = lambday * eq2.dfdx(x, y),
                dphi2dy = 1 + lambday * eq2.dfdy(x, y);
        return dphi1dx * dphi2dy - dphi1dy * dphi2dx;
    }

    public int maxIndex(double[] arr) {
        int result = 0;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > max) {
                result = i;
                max = arr[i];
            }
        }
        return result;
    }

    public record Result(double x, double y, double deltax, double deltay, int iters) {
    }
}
