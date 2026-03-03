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
import yagen.waitmydawn.registries.DataAttachmentRegistry;

import java.util.List;

import static yagen.waitmydawn.player.KeyMappings.ABILITY_1_KEYMAP;
import static yagen.waitmydawn.player.KeyMappings.ABILITY_2_KEYMAP;

public class ThermalSunderArmorMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "thermal_sunder_armor_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel, Player player) {
        if (player == null) {
            return List.of(
                    Component.translatable("warframe.yagens_attributes.active", ABILITY_1_KEYMAP.getTranslatedKeyMessage().getString(), ABILITY_2_KEYMAP.getTranslatedKeyMessage().getString()),
                    Component.translatable("warframe.yagens_attributes.thermal_sunder_armor_mod.1"),
                    Component.translatable("warframe.yagens_attributes.thermal_sunder_armor_mod.2"),
                    Component.translatable("warframe.yagens_attributes.thermal_sunder_armor_mod.3")
            );
        } else {
            double baseDamage = ServerConfigs.MOD_WARFRAME_THERMAL_SUNDER.get() * player.getAttributeValue(YAttributes.ABILITY_STRENGTH);
            double batteryPower = DataAttachmentRegistry.getBatteryPower(player);
            double radius = ServerConfigs.MOD_WARFRAME_THERMAL_SUNDER_RANGE.get() * player.getAttributeValue(YAttributes.ABILITY_RANGE);
            double duration = ServerConfigs.MOD_WARFRAME_THERMAL_SUNDER_DURATION.get() * player.getAttributeValue(YAttributes.ABILITY_DURATION);

            baseDamage = baseDamage * (1 + batteryPower * 0.04);

            return List.of(
                    Component.translatable("warframe.yagens_attributes.active", ABILITY_1_KEYMAP.getTranslatedKeyMessage().getString(), ABILITY_2_KEYMAP.getTranslatedKeyMessage().getString()),
                    Component.translatable("warframe.yagens_attributes.thermal_sunder_armor_mod.1",
                            String.format("%.1f", radius), String.format("%.1f", baseDamage)),
                    Component.translatable("warframe.yagens_attributes.thermal_sunder_armor_mod.2",
                            String.format("%.1f", radius), String.format("%.1f", baseDamage * 2)),
                    Component.translatable("warframe.yagens_attributes.thermal_sunder_armor_mod.3",
                            String.format("%.1f", duration))
            );
        }
    }

    public ThermalSunderArmorMod() {
        super(1, FormaType.NYA.getValue(), ModRarity.WARFRAME);
        this.baseCapacityCost = 9;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public double energyCost() {
        return 75.0;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
