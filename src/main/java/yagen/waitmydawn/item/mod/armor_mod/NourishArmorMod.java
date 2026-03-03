package yagen.waitmydawn.item.mod.armor_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
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

public class NourishArmorMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "nourish_armor_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel, Player player) {
        if (player == null) {
            return List.of(
                    Component.translatable("warframe.yagens_attributes.active", ABILITY_1_KEYMAP.getTranslatedKeyMessage().getString(), ABILITY_2_KEYMAP.getTranslatedKeyMessage().getString()),
                    Component.translatable("warframe.yagens_attributes.nourish_armor_mod.1"),
                    Component.translatable("warframe.yagens_attributes.nourish_armor_mod.2"),
                    Component.translatable("warframe.yagens_attributes.nourish_armor_mod.3")
            );
        } else {
            double maxEnhance = 1 + (ServerConfigs.MOD_WARFRAME_NOURISH_MAX_ENHANCE.get() - 1) * player.getAttributeValue(YAttributes.ABILITY_STRENGTH);
            double duration = ServerConfigs.MOD_WARFRAME_NOURISH_DURATION.get() * player.getAttributeValue(YAttributes.ABILITY_DURATION);
            return List.of(
                    Component.translatable("warframe.yagens_attributes.active", ABILITY_1_KEYMAP.getTranslatedKeyMessage().getString(), ABILITY_2_KEYMAP.getTranslatedKeyMessage().getString()),
                    Component.translatable("warframe.yagens_attributes.nourish_armor_mod.1",
                            String.format("%.1f", duration)),
                    Component.translatable("warframe.yagens_attributes.nourish_armor_mod.2",
                            String.format("%.1f", (maxEnhance - 1) * 100)),
                    Component.translatable("warframe.yagens_attributes.nourish_armor_mod.3")
            );
        }
    }

    public NourishArmorMod() {
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
