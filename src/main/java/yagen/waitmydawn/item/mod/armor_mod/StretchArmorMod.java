package yagen.waitmydawn.item.mod.armor_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.config.ServerConfigs;

import java.util.List;

public class StretchArmorMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "stretch_armor_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel) {
        return List.of(
                Component.translatable("tooltips.yagens_attributes.ability_range_multibase", String.format("%.2f", ServerConfigs.MOD_UNCOMMON_STRETCH.get().floatValue() * modLevel))
        );
    }

    public StretchArmorMod() {
        super(5, "Nya", ModRarity.UNCOMMON);
        this.baseCapacityCost = 4;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
