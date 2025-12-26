package yagen.waitmydawn.api.attribute;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import yagen.waitmydawn.YagensAttributes;

import java.util.HashMap;
import java.util.Map;


public class DefaultItemAttributes {
    // default map
    public static Map<Item, Map<Attribute, Double>> DEFAULTS = new HashMap<>();

    public static void clear() {
        DEFAULTS.clear();
    }

    public static void put(Item item, Map<Attribute, Double> attributes) {
        DEFAULTS.put(item, attributes);
    }

    public static void apply(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;

        // skip if marked
        if (stack.has(DataComponents.CUSTOM_DATA)
                && stack.get(DataComponents.CUSTOM_DATA).contains("yagens_attributes_default_applied")) {
            return;
        }

        var item = stack.getItem();
        var map = DEFAULTS.get(item);
        if (map == null) return;

        // get modifiers
        var existing = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        // copy existed
        final ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        existing.modifiers().forEach(e -> builder.add(e.attribute(), e.modifier(), e.slot()));

        map.forEach((attr, value) -> {
            ResourceLocation key = BuiltInRegistries.ATTRIBUTE.getKey(attr);
            builder.add(
                    BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attr),
                    new AttributeModifier(
                            ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID,
                                    "default_" + key.getPath()),
                            value,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND
            );
        });

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
        // mark
        stack.set(DataComponents.CUSTOM_DATA,
                stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                        .update(tag -> tag.putBoolean("yagens_attributes_default_applied", true)));
    }
}
