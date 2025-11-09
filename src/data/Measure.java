package data;


public class Measure implements Variant{
    public double magnitude;
    public int meter;
    public int second;

    public static final double EQUALITY_THRESHOLD = 2e-8;
    public static final int SIGNIFICANT_FIGURES = 5;
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

    public boolean equals(Measure other) {
        return (
                other.meter == this.meter
            &&  other.second == this.second
            &&  Math.abs(this.magnitude - other.magnitude) <= EQUALITY_THRESHOLD
        );
    }

    @Override
    public String toString() {
        String result = "";

        if (Math.abs(magnitude - Math.round(magnitude)) <= EQUALITY_THRESHOLD) result += Math.round(magnitude);
        else result += roundToSignificantFigures(magnitude, SIGNIFICANT_FIGURES);

        if (meter != 0) {
            if (meter < 0) result += "p";
            result += Math.abs(meter);
        }

        if (second != 0) {
            if (second < 0) result += "p";
            result += Math.abs(second);
        }

        return result;
    }

    @Override
    public Measure operate(Variant other, OperationType type, OperationDirection direction) {
        if (!(other instanceof Measure otherMeasure)) throw new UnsupportedOperationException("Can only compare operate on measures with other measures");

        switch (type) {
            case OperationType.LINEAR -> {
                if (meter != otherMeasure.meter || second != otherMeasure.second) throw new UnsupportedOperationException("Can only compare operate on measures with matching dimensions");

                double delta = otherMeasure.magnitude - magnitude;
                switch (direction) {
                    case OperationDirection.POSITIVE -> {}
                    case OperationDirection.NEUTRAL -> { delta *= 0.0; }
                    case OperationDirection.NEGATIVE -> { delta *= -1.0; }
                }

                return new Measure(delta, meter, second);
            }

            case OperationType.PROPORTIONAL -> {
                int meterDelta = otherMeasure.meter - meter;
                int secondDelta = otherMeasure.second - second;
                
                double multiplier = otherMeasure.magnitude;

                switch (direction) {
                    case OperationDirection.POSITIVE -> { }
                    case OperationDirection.NEUTRAL -> { multiplier = 0.0; }
                    case OperationDirection.NEGATIVE -> { 
                        multiplier = 1.0 / multiplier;
                        meterDelta *= -1.0;
                        secondDelta *= -1.0; 
                    }

                }

                return new Measure(magnitude * multiplier, meterDelta, secondDelta);
            }

            default -> {
                throw new UnsupportedOperationException("Other operation types not yet supported");
            }
        }
    }   

    static double roundToSignificantFigures(double num, int n) {
        if (num == 0) {
            return 0;
        }

        final double d = Math.ceil(Math.log10(Math.abs(num)));
        final int power = n - (int) d;
        final double magnitude = Math.pow(10, power);
        final long shifted = Math.round(num * magnitude);
        return shifted / magnitude;
    }

    public boolean isTrue() {
        return !equals(Measure.FALSE);
    }
}
