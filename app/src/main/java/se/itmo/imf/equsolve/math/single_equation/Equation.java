package se.itmo.imf.equsolve.math.single_equation;

import java.util.function.Function;

record Equation(Function<Double, Double> f,
                Function<Double, Double> df,
                Function<Double, Double> ddf) {
}