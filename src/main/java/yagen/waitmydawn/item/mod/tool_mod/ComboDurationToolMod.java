package yagen.waitmydawn.item.mod.tool_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.item.FormaType;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.config.ServerConfigs;

import java.util.List;

public class ComboDurationToolMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "combo_duration_tool_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel) {
        return List.of(
                Component.translatable("tooltips.yagens_attributes.combo_duration_add", String.format("%.2f", ServerConfigs.MOD_RARE_HARVEST_TIME.get().floatValue() * modLevel))
        );
    }

    public ComboDurationToolMod() {
        super(5, FormaType.NYA.getValue(), ModRarity.RARE);
        this.baseCapacityCost = 4;
    }

    @Override
    public boolean isReservoir() { return true; }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
