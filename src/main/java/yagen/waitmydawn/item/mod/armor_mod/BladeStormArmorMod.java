package yagen.waitmydawn.item.mod.armor_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.YAttributes;
import yagen.waitmydawn.api.item.FormaType;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.config.ServerConfigs;

import java.util.List;

import static yagen.waitmydawn.player.KeyMappings.ABILITY_1_KEYMAP;
import static yagen.waitmydawn.player.KeyMappings.ABILITY_2_KEYMAP;

public class BladeStormArmorMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "blade_storm_armor_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel, Player player) {
        if (player == null) {
            return List.of(
                    Component.translatable("warframe.yagens_attributes.active", ABILITY_1_KEYMAP.getTranslatedKeyMessage().getString(), ABILITY_2_KEYMAP.getTranslatedKeyMessage().getString()),
                    Component.translatable("warframe.yagens_attributes.blade_storm_armor_mod.1"),
                    Component.translatable("warframe.yagens_attributes.blade_storm_armor_mod.2")
            );
        } else {
            double duration = ServerConfigs.MOD_WARFRAME_BLADE_STORM_DURATION.get() * player.getAttributeValue(YAttributes.ABILITY_DURATION);
            double range = ServerConfigs.MOD_WARFRAME_BLADE_STORM_RANGE.get() * player.getAttributeValue(YAttributes.ABILITY_RANGE);
            return List.of(
                    Component.translatable("warframe.yagens_attributes.active", ABILITY_1_KEYMAP.getTranslatedKeyMessage().getString(), ABILITY_2_KEYMAP.getTranslatedKeyMessage().getString()),
                    Component.translatable("warframe.yagens_attributes.blade_storm_armor_mod.1",
                            String.format("%.1f", duration),
                            String.format("%.1f", range)),
                    Component.translatable("warframe.yagens_attributes.blade_storm_armor_mod.2")
            );
        }
    }

    public BladeStormArmorMod() {
        super(1, FormaType.CTH.getValue(), ModRarity.WARFRAME);
        this.baseCapacityCost = 9;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public double energyCost() {
        return 100.0;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
