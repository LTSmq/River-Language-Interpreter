import java.util.regex.*;

public class Measure {
    private static final double FLOATING_POINT_THRESHOLD = 1e-10;
    double magnitude;
    int meter;
    int second;

    public static final Measure trueValue = new Measure(1.0);
    public static final Measure falseValue = new Measure(0.0);
    public static Measure booleanOf(boolean primitive) {
        return primitive ? trueValue : falseValue;
    }

    /* Potential other inclusions:

    int kilograms;

    */

    static final Pattern measurePattern = Pattern.compile(
       "\\b(?<value>\\d+(?:\\.\\d+)?)(?:(?<meterInverter>p)?(?<meterModifier>[km])?(?<meter>m)(?<meterPower>\\d+)?)?(?:(?<timeInverter>p)?(?<time>[dhs])(?<timePower>\\d+)?)?\\b"
    );
    public static Measure parse(String measureString) {

        // Perform search
        Matcher matcher = measurePattern.matcher(measureString);
        if (!matcher.find()) return null;

        // Find value
        String valueString = matcher.group("value");
        if (valueString == null) return null;
        double value;
        try {
            value = Double.parseDouble(valueString);
        }
        catch (NumberFormatException nfe) {
            return null;
        }

        // Initialise default powers
        int meterPower = 0;
        int secondPower = 0;

        // Find meter value
        if (matcher.group("meter") != null) {
            // Apply modifier to value (kilometers, milimeters)
            String modifier = matcher.group("meterModifier");
            if (modifier != null) {
                if (modifier.equals("k")) {
                    value *= 1_000.0;
                }
                else if (modifier.equals("m")) {
                    value /= 1_000.0;
                }
            }

            // Find power
            String powerString = matcher.group("meterPower");
            meterPower = (powerString == null) ? 1 : Integer.parseInt(powerString);

            // Apply inverter
            if (matcher.group("meterInverter") != null) meterPower *= -1; 
        }

        // Find second value
        String timeUnit = matcher.group("time"); 
        if (timeUnit != null) {
            // Find power
            String powerString = matcher.group("timePower");
            secondPower = (powerString == null) ? 1 : Integer.parseInt(powerString);

            // Convert to seconds
            if (timeUnit.equals("h")) {
                value *= Math.pow(3_600.0, secondPower);
            }
            else if (timeUnit.equals("d")) {
                value *= Math.pow(86_400.0, secondPower);
            }

            // Apply inverter
            if (matcher.group("timeInverter") != null) secondPower *= -1;
        }

        return new Measure(value, meterPower, secondPower);
    }

    private static boolean equal(double value_a, double value_b) {
        return Math.abs(value_a - value_b) <= FLOATING_POINT_THRESHOLD;
    }
    
    public Measure() {
        ;
    }

    public Measure(double given_magnitude) {
        magnitude = given_magnitude;
    }

    public Measure(double given_magnitude, int given_meter, int given_second) {
        magnitude = given_magnitude;
        meter = given_meter;
        second = given_second;

    }

    public Measure(Measure other) {
        magnitude = other.magnitude;
        meter = other.meter;
        second = other.second;
    }
    

    Measure multiply(Measure other) {
        return new Measure(
            magnitude * other.magnitude,
            meter + other.meter,
            second + other.second
        );
    }

    Measure multiply(double value) {
        return multiply(new Measure(value));
    }

    Measure divide(Measure other) {
        if (equal(other.magnitude, 0.0)){
            throw new ArithmeticException("Divided a measure by zero");
        } 
        return new Measure(
            magnitude / other.magnitude,
            meter - other.meter,
            second - other.second
        );
    }

    Measure add(Measure other) {
        if (meter != other.meter || second != other.second) {
            throw new UnsupportedOperationException("Cannot perform addition between nonmatching types");
        }
        return new Measure(magnitude + other.magnitude, meter, second);
    }

    Measure subtract(Measure other) {
        return add(other.multiply(-1.0));
    }

    boolean comparable(Measure other) {
        return (meter == other.meter) && (second == other.second);
    }

    int compared_to(Measure other) {
        if (!comparable(other)) return 0;
        if (equal(magnitude, other.magnitude)) return 0;
        if (magnitude - other.magnitude > 0.0) return 1;
        else return -1;

    }

    boolean equals(Measure other) {
        if (!comparable(other)) return false;

        return equal(magnitude, other.magnitude);
    }

    @Override
    public String toString() {
        String meter_string = "";
        if (meter != 0) {
            meter_string = "m";
            if (meter > 1) {
                meter_string = meter_string + Integer.toString(Math.abs(meter));
            }
            if (meter < 0) {
                meter_string = "p" + meter_string;
            }
        }

        String second_string = "";
        if (second != 0) {
            second_string = "s";
            if (second > 1) {
                second_string += Integer.toString(Math.abs(meter));
            }
            if (second < 0) {
                second_string = "p" + second_string;
            }
        }
        return Double.toString(Math.round(magnitude * 100) / 100) + meter_string + second_string;
    }
}
