package yagen.waitmydawn.item.mod.armor_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.config.ServerConfigs;

import java.util.List;

public class HunterAdrenalineArmorMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "hunter_adrenaline_armor_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel) {
        return List.of(
                Component.translatable("functions.yagens_attributes.rage_armor_mod.1", String.format("%.0f", ServerConfigs.MOD_COMMON_HUNTER_ADRENALINE.get().floatValue() * 100 * modLevel)),
                Component.translatable("functions.yagens_attributes.hunter_set.1", String.format("%.1f", ServerConfigs.MOD_SET_HUNTER.get().floatValue()))
        );
    }

    public HunterAdrenalineArmorMod() {
        super(5, "Cth", ModRarity.COMMON);
        this.baseCapacityCost = 6;
    }

    @Override
    public boolean isHunterSet() {
        return true;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
