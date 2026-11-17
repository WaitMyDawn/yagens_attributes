package yagen.waitmydawn.yagens_attributes.compat;

import net.minecraft.world.item.*;
import yagen.waitmydawn.yagens_attributes.core.AttackKind;
import java.util.*;
import java.util.function.ToDoubleFunction;

/** Explicit item capabilities supplied after optional-mod registries have completed. */
public final class NativeWeapons {
    public record Adapter(AttackKind kind, boolean capture, boolean volley, int countBaseline, int period,
                          ToDoubleFunction<ItemStack> baseline) {
        public Adapter {
            Objects.requireNonNull(kind);
            if (kind == AttackKind.UNKNOWN || countBaseline < 0 || countBaseline > 64 || period < 0 || period == 1
                    || !capture && (volley || countBaseline != 0 || period != 0) || countBaseline > 0 && !volley)
                throw new IllegalArgumentException("Invalid native weapon adapter");
        }
    }
    private static final Map<Item, Adapter> ADAPTERS = new IdentityHashMap<>();
    private NativeWeapons() {}
    public static void register(Item item, Adapter adapter) {
        if (ADAPTERS.putIfAbsent(item, adapter) != null) throw new IllegalStateException("Duplicate native weapon adapter");
    }
    public static Adapter adapter(ItemStack stack) { return ADAPTERS.get(stack.getItem()); }
    public static boolean ranged(ItemStack stack) {
        var adapter = adapter(stack); return adapter != null && adapter.kind() == AttackKind.RANGED;
    }
    public static boolean continuous(ItemStack stack) { var adapter = adapter(stack); return adapter != null && adapter.period() > 0; }
    public static OptionalDouble baseline(ItemStack stack) {
        var adapter = adapter(stack);
        return adapter == null || adapter.baseline() == null ? OptionalDouble.empty() : OptionalDouble.of(adapter.baseline().applyAsDouble(stack));
    }
}
