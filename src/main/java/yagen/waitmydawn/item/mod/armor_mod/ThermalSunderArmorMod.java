package yagen.waitmydawn.item.mod.armor_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.item.FormaType;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;

import java.util.List;

import static yagen.waitmydawn.player.KeyMappings.ABILITY_1_KEYMAP;
import static yagen.waitmydawn.player.KeyMappings.ABILITY_2_KEYMAP;

public class ThermalSunderArmorMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "thermal_sunder_armor_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel) {
        return List.of(
                Component.translatable("warframe.yagens_attributes.active", ABILITY_1_KEYMAP.getTranslatedKeyMessage().getString(), ABILITY_2_KEYMAP.getTranslatedKeyMessage().getString()),
                Component.translatable("warframe.yagens_attributes.thermal_sunder_armor_mod.1"),
                Component.translatable("warframe.yagens_attributes.thermal_sunder_armor_mod.2")
        );
    }

    public ThermalSunderArmorMod() {
        super(1, FormaType.NYA.getValue(), ModRarity.WARFRAME);
        this.baseCapacityCost = 9;
    }

    @Override
    public boolean isActive() { return true;}

    @Override
    public double energyCost() {
        return 25.0;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
