package data;

public class Measure {
    public double magnitude;
    public int meter;
    public int second;

    public static final Measure FALSE = new Measure(0.0);
    public static final Measure TRUE = new Measure(1.0);

    public Measure(double magnitude, int meter, int second) {
        this.magnitude = magnitude;
        this.meter = meter;
        this.second = second;
    }

    public Measure(double magnitude) {
        this(magnitude, 0, 0);
    }
}
