package se.itmo.imf.equsolve.math.approximation;

import java.util.List;

class Approximator {
    private final List<Approximation> approximations;

    private Approximation best = null;

    public Approximator(TableFunction func) {
        approximations = List.of(
            new Approximations.Linear(func),
            new Approximations.Quadratic(func),
            new Approximations.Cubic(func),
            new Approximations.Exponential(func),
            new Approximations.Logarithmic(func),
            new Approximations.Power(func)
        );
    }

    public List<Approximation> getAll() {
        return approximations;
    }

    public Approximation getBest() {
        if (best != null) {
            return best;
        }

        double minStdDev = Double.MAX_VALUE;
        for (Approximation approximation : approximations) {
            double stdDev = approximation.getStdDev();
            if (stdDev < minStdDev) {
                minStdDev = stdDev;
                best = approximation;
            }
        }

        return best;
    }
}
