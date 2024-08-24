package se.itmo.imf.equsolve.math.integral;

import static java.lang.StrictMath.*;

import se.itmo.imf.equsolve.Func;

record Function(String latex, String jessieCode, Func f) {
    double f(double x) { return f.apply(x); }

    static Function[] FUNCTIONS = new Function[]{
            new Function("\\int_a^b{x^2dx}", "x^2", x -> pow(x, 2)),
            new Function("\\int_a^b{\\sin(x)dx}", "sin(x)", x -> sin(x)),
    };
}
