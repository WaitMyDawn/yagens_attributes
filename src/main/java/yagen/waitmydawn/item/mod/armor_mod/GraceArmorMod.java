package yagen.waitmydawn.item.mod.armor_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.registries.ComponentRegistry;


import java.util.*;

public class GraceArmorMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "grace_armor_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel) {
        return List.of(
                Component.translatable("functions.yagens_attributes.grace_armor_mod.1",
                        String.format("%.2f", 0.1 + 0.05 * modLevel))
        );
    }

    public GraceArmorMod() {
        super(5, "Nya", ModRarity.RARE);
        this.baseCapacityCost = 4;
    }

    public static void setGraceAbility(Attribute attribute, ItemStack stack) {
        stack.set(ComponentRegistry.GRACE_ABILITY.get(), attribute);
    }

    public static Attribute getGraceAbility(ItemStack stack) {
        return stack.getOrDefault(ComponentRegistry.GRACE_ABILITY.get(), Attributes.MOVEMENT_SPEED.value());
    }

    public static void setRandomGraceAbility(ItemStack stack) {
        Random random = new Random();
        List<Attribute> attributeList = new ArrayList<>(ATTRIBUTE_SET.keySet());
        setGraceAbility(attributeList.get(random.nextInt(attributeList.size())), stack);
    }

    public static Map<Attribute, Double> ATTRIBUTE_SET =new HashMap<>();

    public static double getBonus(Attribute attribute, double overflow) {
        double modifier = ATTRIBUTE_SET.get(attribute);
        double bonus = 0.0;

        if (overflow > 0.0)
            bonus = bonus + Math.min(overflow, 0.3) / 0.3;
        if (overflow > 0.3)
            bonus = bonus + Math.min(overflow - 0.3, 2.4) / 2.4;
        if (overflow > 2.7)
            bonus = bonus + 1 - Math.exp(-(overflow - 2.7) / 2.7);

        return bonus * modifier;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
