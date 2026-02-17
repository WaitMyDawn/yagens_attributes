package yagen.waitmydawn.item.mod.armor_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.config.ServerConfigs;

import java.util.List;

public class OverextendedArmorMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "overextended_armor_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel) {
        return List.of(
                Component.translatable("tooltips.yagens_attributes.ability_range_multibase", String.format("%.2f", ServerConfigs.MOD_RARE_OVEREXTENDED_RANGE.get().floatValue() * modLevel)),
                Component.translatable("tooltips.yagens_attributes.ability_strength_multibaseneg", String.format("%.2f", ServerConfigs.MOD_RARE_OVEREXTENDED_STRENGTH.get().floatValue() * modLevel))
        );
    }

    public OverextendedArmorMod() {
        super(5, "Nod", ModRarity.RARE);
        this.baseCapacityCost = 6;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
