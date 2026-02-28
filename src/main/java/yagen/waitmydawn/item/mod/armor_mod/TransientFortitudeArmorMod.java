package yagen.waitmydawn.item.mod.armor_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.config.ServerConfigs;

import java.util.List;

public class TransientFortitudeArmorMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "transient_fortitude_armor_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel) {
        return List.of(
                Component.translatable("tooltips.yagens_attributes.ability_strength_multibase", String.format("%.2f", ServerConfigs.MOD_RARE_TRANSIENT_FORTITUDE_STRENGTH.get().floatValue() * modLevel)),
                Component.translatable("tooltips.yagens_attributes.ability_duration_multibaseneg", String.format("%.2f", ServerConfigs.MOD_RARE_TRANSIENT_FORTITUDE_DURATION.get().floatValue() * modLevel))
        );
    }

    public TransientFortitudeArmorMod() {
        super(10, "Cth", ModRarity.RARE);
        this.baseCapacityCost = 6;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
