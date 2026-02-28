package yagen.waitmydawn.item.mod.armor_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.config.ServerConfigs;

import java.util.List;

public class NarrowMindedArmorMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "narrow_minded_armor_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel) {
        return List.of(
                Component.translatable("tooltips.yagens_attributes.ability_duration_multibase", String.format("%.2f", ServerConfigs.MOD_RARE_NARROW_MINDED_DURATION.get().floatValue() * modLevel)),
                Component.translatable("tooltips.yagens_attributes.ability_range_multibaseneg", String.format("%.2f", ServerConfigs.MOD_RARE_NARROW_MINDED_RANGE.get().floatValue() * modLevel))
        );
    }

    public NarrowMindedArmorMod() {
        super(10, "Nod", ModRarity.RARE);
        this.baseCapacityCost = 6;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
