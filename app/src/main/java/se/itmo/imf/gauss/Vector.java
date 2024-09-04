package se.itmo.imf.gauss;

import java.util.Arrays;

public class Vector {
    private final double[] data;

    /*
    Constructors
     */
    public Vector(double... data) {
        this.data = data;
    }

    public static Vector zero(int size) {
        return new Vector(new double[size]);
    }

    public static Vector random(int size) {
        double[] data = new double[size];
        for (int i = 0; i < size; i++) {
            data[i] = (double) Math.random();
        }
        return new Vector(data);
    }

    /*
    Head/tail
     */
    public Vector dropFirst(int n) {
        return new Vector(Arrays.copyOfRange(data, n, size()));
    }

    public Vector takeFirst(int n) {
        return new Vector(Arrays.copyOfRange(data, 0, n));
    }

    /*
    Basic access
     */
    public int size() {
        return this.data.length;
    }

    protected double[] getData() {
        return data;
    }

    public double get(int index) {
        return data[index];
    }


    private void assertSize(Vector other) {
        if (other.size() != this.size()) {
            throw new IllegalArgumentException(
                String.format("incompatible vector sizes: this=%d, other=%d", other.size(), this.size()));
        }
    }

    /*
    Arithmetic, returns a copy
     */
    public Vector add(Vector other) {
        assertSize(other);

        Vector newVector = clone();
        for (int i = 0; i < data.length; i++) {
            newVector.data[i] += other.data[i];
        }
        return newVector;
    }

    public Vector mul(Vector other) {
        assertSize(other);

        Vector newVector = clone();
        for (int i = 0; i < size(); i++) {
            newVector.data[i] *= other.data[i];
        }
        return newVector;
    }

    public Vector mul(double x) {
        Vector res = clone();
        res.doInplace().mul(x);
        return res;
    }

    public double dot(Vector other) {
        assertSize(other);

        Vector mul = mul(other);
        double sum = 0;
        for (int i = 0; i < size(); i++) {
            sum += mul.data[i];
        }
        return sum;
    }

    public double[] asArray() {
        return Arrays.copyOf(data, data.length);
    }

    /*
    Special facility for in-place modification (to avoid many copies)
     */
    public VectorInplace doInplace() {
        return new VectorInplaceImpl();
    }

    public interface VectorInplace {
        VectorInplace add(Vector other);
        VectorInplace mul(double x);
    }

    private class VectorInplaceImpl implements VectorInplace {
        public VectorInplaceImpl add(Vector other) {
            assertSize(other);

            for (int i = 0; i < Vector.this.data.length; i++) {
                Vector.this.data[i] += other.data[i];
            }
            return this;
        }

        public VectorInplaceImpl mul(double x) {
            for (int i = 0; i < Vector.this.data.length; i++) {
                Vector.this.data[i] *= x;
            }
            return this;
        }
    }

    @Override
    public Vector clone() {
        return new Vector(data.clone());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[ ");
        for (double f : data) {
            sb.append(f);
            sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }
}
