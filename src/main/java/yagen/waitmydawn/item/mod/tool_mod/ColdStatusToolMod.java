package yagen.waitmydawn.item.mod.tool_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.config.ServerConfigs;

import java.util.List;

public class ColdStatusToolMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "cold_status_tool_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel) {
        return List.of(
                Component.translatable("tooltip.yagens_attributes.cold_addition", String.format("%.2f", ServerConfigs.MOD_RARE_STATUS_ELEMENT.get().floatValue() * modLevel)),
                Component.translatable("tooltips.yagens_attributes.status_chance_multibase", String.format("%.2f", ServerConfigs.MOD_RARE_STATUS_ELEMENT.get().floatValue() * modLevel))
        );
    }

    public ColdStatusToolMod() {
        super(5, "Cth", ModRarity.RARE);
        this.baseCapacityCost = 4;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
