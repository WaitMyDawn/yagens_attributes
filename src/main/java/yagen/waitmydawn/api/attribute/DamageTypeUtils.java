package yagen.waitmydawn.api.attribute;

import net.minecraft.world.item.ItemStack;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.registries.ComponentRegistry;

import java.util.Map;

public class DamageTypeUtils {
    public static Map<DamageType, Float> getDamageTypes(ItemStack stack) {
        if (!IModContainer.isModContainer(stack)) {
            Map<DamageType, Float> vanilla = DefaultDamageTypeRegistry.get(stack.getItem());
            if (vanilla != null) return vanilla;
            return Map.of();
        }
        IModContainer container = stack.get(ComponentRegistry.MOD_CONTAINER.get());
        if (container != null && !container.getDamageProfile().isEmpty()) {
            return container.getDamageProfile();
        }
        Map<DamageType, Float> vanilla = DefaultDamageTypeRegistry.get(stack.getItem());
        if (vanilla != null) return vanilla;
        return Map.of();
    }
}