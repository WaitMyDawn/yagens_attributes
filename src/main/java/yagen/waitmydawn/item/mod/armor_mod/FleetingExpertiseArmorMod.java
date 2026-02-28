package yagen.waitmydawn.item.mod.armor_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.config.ServerConfigs;

import java.util.List;

public class FleetingExpertiseArmorMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "fleeting_expertise_armor_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel) {
        return List.of(
                Component.translatable("tooltips.yagens_attributes.ability_efficiency_multibase", String.format("%.2f", ServerConfigs.MOD_RARE_FLEETING_EXPERTISE_EFFICIENCY.get().floatValue() * modLevel)),
                Component.translatable("tooltips.yagens_attributes.ability_duration_multibaseneg", String.format("%.2f", ServerConfigs.MOD_RARE_FLEETING_EXPERTISE_DURATION.get().floatValue() * modLevel))
        );
    }

    public FleetingExpertiseArmorMod() {
        super(6, "Nya", ModRarity.RARE);
        this.baseCapacityCost = 5;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
