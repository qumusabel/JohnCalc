package se.itmo.imf.equsolve.solvers.single;

import java.util.function.Function;

record Equation(Function<Double, Double> f,
                Function<Double, Double> df,
                Function<Double, Double> ddf) {
}