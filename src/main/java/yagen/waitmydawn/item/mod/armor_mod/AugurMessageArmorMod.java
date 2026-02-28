package yagen.waitmydawn.item.mod.armor_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.config.ServerConfigs;

import java.util.List;

public class AugurMessageArmorMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "augur_message_armor_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel) {
        return List.of(
                Component.translatable("tooltips.yagens_attributes.ability_duration_multibase", String.format("%.2f", ServerConfigs.MOD_COMMON_AUGUR_MESSAGE.get().floatValue() * modLevel)),
                Component.translatable("functions.yagens_attributes.augur_set.1", String.format("%.1f", ServerConfigs.MOD_SET_AUGUR.get().floatValue()))
        );
    }

    public AugurMessageArmorMod() {
        super(5, "Nya", ModRarity.COMMON);
        this.baseCapacityCost = 2;
    }

    @Override
    public boolean isAugurSet() {
        return true;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
