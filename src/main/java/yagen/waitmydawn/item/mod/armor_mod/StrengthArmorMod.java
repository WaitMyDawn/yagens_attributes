package yagen.waitmydawn.item.mod.armor_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.config.ServerConfigs;

import java.util.List;

public class StrengthArmorMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "strength_armor_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel) {
        return List.of(
                Component.translatable("tooltips.yagens_attributes.ability_strength_multibase", String.format("%.2f", ServerConfigs.MOD_RARE_MEDITATION_RUNE.get().floatValue() * modLevel))
        );
    }

    public StrengthArmorMod() {
        super(5, "Cth", ModRarity.RARE);
        this.baseCapacityCost = 6;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
