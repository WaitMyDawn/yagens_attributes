package yagen.waitmydawn.yagens_attributes.core;

import java.util.List;

/**
 * Addition values are increments; independent multipliers are complete factors.
 */
public record Bonus(double addin, double addition, List<Double> multiply, double addout, double inputAddin, double inputAddition) {
    public Bonus(double addin, double addition, List<Double> multiply, double addout) { this(addin, addition, multiply, addout, 0, 0); }
    public Bonus(double addin, double addition, List<Double> multiply, double addout, double inputAddin) { this(addin, addition, multiply, addout, inputAddin, 0); }
    public static final Bonus NONE = new Bonus(0, 0, List.of(), 0);

    public Bonus {
        multiply = List.copyOf(multiply);
        finite(addin);
        finite(addition);
        finite(addout);
        finite(inputAddin);
        finite(inputAddition);
        for (double factor : multiply) finite(factor);
    }

    public static double evaluate(double base, Iterable<Bonus> bonuses) {
        return evaluate(base, bonuses, 0);
    }

    /** Relative coefficients retain their declared basis and participate in the matching modifier stage. */
    public static double evaluate(double base, Iterable<Bonus> bonuses, double input) {
        finite(base);
        finite(input);
        double in = 0, addition = 1, relativeAddition = 0, product = 1, out = 0;
        for (Bonus bonus : bonuses) {
            in += bonus.addin + bonus.inputAddin * input;
            addition += bonus.addition;
            relativeAddition += bonus.inputAddition;
            for (double factor : bonus.multiply) product *= factor;
            out += bonus.addout;
        }
        double result = ((base + in) * addition + input * relativeAddition) * product + out;
        finite(result);
        return result;
    }

    private static void finite(double value) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Non-finite stat value");
    }
}
