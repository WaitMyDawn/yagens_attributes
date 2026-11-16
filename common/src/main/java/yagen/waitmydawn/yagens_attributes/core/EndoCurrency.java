package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

/**
 * Integer-only accounting and preflight allocation keep rejected transfers free of mutations.
 */
public final class EndoCurrency {
    public record Withdrawal(long balance, List<Integer> additions) {
        public Withdrawal {
            additions = List.copyOf(additions);
        }
    }

    private EndoCurrency() {
    }

    public static long credit(long balance, long amount) {
        if (balance < 0 || amount <= 0) throw new IllegalArgumentException("gui.yagens_attributes.endo_invalid_amount");
        if (amount > Long.MAX_VALUE - balance)
            throw new IllegalArgumentException("gui.yagens_attributes.endo_overflow");
        return balance + amount;
    }

    public static OptionalLong parse(String input, long maximum) {
        if (input.isEmpty() || input.length() > 19 || input.chars().anyMatch(c -> c < '0' || c > '9'))
            return OptionalLong.empty();
        try {
            long value = Long.parseLong(input);
            return value > 0 && value <= maximum ? OptionalLong.of(value) : OptionalLong.empty();
        } catch (NumberFormatException exception) {
            return OptionalLong.empty();
        }
    }

    public static Withdrawal withdraw(long balance, long amount, List<Integer> room) {
        if (balance < 0 || amount <= 0) throw new IllegalArgumentException("gui.yagens_attributes.endo_invalid_amount");
        if (amount > balance) throw new IllegalArgumentException("gui.yagens_attributes.endo_insufficient");
        long capacity = 0;
        for (int space : room) {
            if (space < 0) throw new IllegalArgumentException("Negative inventory space");
            capacity += space;
        }
        if (amount > capacity) throw new IllegalArgumentException("gui.yagens_attributes.endo_inventory_full");
        long remaining = amount;
        List<Integer> additions = new ArrayList<>();
        for (int space : room) {
            int count = (int) Math.min(remaining, space);
            additions.add(count);
            remaining -= count;
        }
        return new Withdrawal(balance - amount, additions);
    }
}
