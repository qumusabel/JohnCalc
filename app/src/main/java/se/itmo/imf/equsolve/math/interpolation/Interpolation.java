package se.itmo.imf.equsolve.math.interpolation;

interface Interpolation {
    double at(double x);
    boolean willBePreciseAt(double x);
}
