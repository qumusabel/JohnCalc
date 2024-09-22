package se.itmo.imf.equsolve.math.interpolation;

record TableFunction(Point[] points) {
    public record Point(double x, double y) {}

    public int numPoints() {
        return points.length;
    }
}