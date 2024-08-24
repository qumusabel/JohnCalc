package se.itmo.imf.equsolve.math.two_equations;

import se.itmo.imf.equsolve.BiFunc;

record BiEquation(String jessieCode, BiFunc f, BiFunc dfdx, BiFunc dfdy) {
    public double f(double x, double y) {
        return f.apply(x, y);
    }

    public double dfdx(double x, double y) {
        return dfdx.apply(x, y);
    }

    public double dfdy(double x, double y) {
        return dfdy.apply(x, y);
    }
}
