package yagen.waitmydawn.yagens_attributes.core;

import java.math.BigInteger;

/** The refund is derived from rank, not a mutable history of payments or Creative upgrades. */
public final class ModRecycling {
    private ModRecycling() {}
    public static long value(int baseEndo, int level) {
        if (baseEndo < 1 || baseEndo > 1_000_000 || level < 0 || level > 60) throw new IllegalArgumentException("Invalid recycling inputs");
        if (baseEndo > 63) throw new IllegalArgumentException("Recycling base exceeds account precision");
        var base = BigInteger.ONE.shiftLeft(baseEndo - 1);
        var investment = BigInteger.ONE.shiftLeft(level).subtract(BigInteger.ONE).multiply(BigInteger.valueOf(baseEndo));
        try { return base.add(investment.divide(BigInteger.TWO)).longValueExact(); }
        catch (ArithmeticException exception) { throw new IllegalArgumentException("Recycling value overflow", exception); }
    }
}
