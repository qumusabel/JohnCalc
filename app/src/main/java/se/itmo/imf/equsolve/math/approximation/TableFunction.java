package se.itmo.imf.equsolve.math.approximation;

record TableFunction(Point[] points) {
    record Point(double x, double y) {}

    public int numPoints() {
        return points.length;
    }
}