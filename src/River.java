import java.util.List;
import java.util.ArrayList;

public class River {
    static Measure area =       new Measure(0.0, 2, 0);
    static Measure distance =   new Measure(0.0, 1, 0);
    static Measure flow =       new Measure(0.0, 3, 2);
    List<River> upstream;
    Measure catchment;

    River(double catchment_area) {
        catchment = new Measure(catchment_area, 2, 0);
        upstream = new ArrayList<>();
    }

    River(double catchment_area, List<River> given_upsteram) {
        catchment = new Measure(catchment_area, 2, 0);
        upstream = new ArrayList<>();
        upstream.addAll(given_upsteram);
    }


}
