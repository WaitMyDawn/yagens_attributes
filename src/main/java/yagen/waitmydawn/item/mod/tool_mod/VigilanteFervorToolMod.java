package yagen.waitmydawn.item.mod.tool_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.config.ServerConfigs;

import java.util.List;

public class VigilanteFervorToolMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "vigilante_fervor_tool_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel) {
        return List.of(
                Component.translatable("tooltips.yagens_attributes.firerate_multibase", String.format("%.2f", ServerConfigs.MOD_UNCOMMON_VIGILANTE_FERVOR.get().floatValue() * modLevel)),
                Component.translatable("functions.yagens_attributes.vigilante_set.1", String.format("%.1f", ServerConfigs.MOD_SET_VIGILANTE.get().floatValue()))

        );
    }

    public VigilanteFervorToolMod() {
        super(5, "Cth", ModRarity.UNCOMMON);
        this.baseCapacityCost = 4;
    }

    @Override
    public boolean isReservoir() { return true; }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
