package aboutlocal.analysis.core.workers;

/**
 * Data structure for storing three objects as a triple.
 */
public class Triple<X, Y, Z> {
    private final X first;
    private final Y second;
    private final Z third;

    public Triple(X t, Y u, Z v) {
        this.first = t;
        this.second = u;
        this.third = v;
    }

    public X getFirst() {
        return first;
    }

    public Y getSecond() {
        return second;
    }

    public Z getThird() {
        return third;
    }

}
