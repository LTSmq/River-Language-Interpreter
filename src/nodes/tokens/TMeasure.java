package nodes.tokens;

import java.util.regex.Matcher;

import data.Measure;

// A measure represents a numeric value in the language
// In particular it also contains 2 integer values representing powers of space and time which can be used for environmental modeling 
// Generates a Measure datatype which is used in evaluation
public class TMeasure extends Token {
    static final double SECONDS_PER_HOUR = 3_600.0;
    static final double SECONDS_PER_DAY = 86_400.0;

    public Measure measure;

    @Override
    public String[] tokenNames() {
        return new String[] {"Measure"};
    }

    public TMeasure(Matcher match) {
        super(match);
        if (match == null) return;
            
        int meter = 0;
        int second = 0;

        // Direct magnitude extraction
        double magnitude = Double.parseDouble(match.group("magnitude"));

        // Extract meter value
        if (match.group("meter") != null) {
            // Apply modifier to meters (kilometers / millimeters)
            String meterModifier = match.group("meterModifier");
            if (meterModifier != null) {
                if      (meterModifier.equals("m")) magnitude *= 0.001;
                else if (meterModifier.equals("k")) magnitude *= 1_000.0;
            }
            
            // Extract meter power
            String meterPower = match.group("meterPower");
            if (meterPower == null) meter = 1;
            else meter = Integer.parseInt(meterPower);
        }

        // Extract second value
        String time = match.group("time");
        if (time != null) {
            // Convert time to seconds
            if      (time.equals("h")) magnitude *= SECONDS_PER_HOUR;
            else if (time.equals("d")) magnitude *= SECONDS_PER_DAY;

            // Extract time power
            String timePower = match.group("timePower");
            if (timePower == null) second = 0;
            else second = Integer.parseInt(timePower); 
        }

        measure = new Measure(magnitude, meter, second);
    }

    @Override 
    public boolean equals(Token other) {
        return other instanceof TMeasure tmeasure && tmeasure.measure.equals(this.measure);
    }
}
