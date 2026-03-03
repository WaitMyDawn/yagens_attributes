package yagen.waitmydawn.item.mod.tool_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.config.ServerConfigs;

import java.util.List;

public class VigilanteArmamentsToolMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "vigilante_armaments_tool_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel, Player player) {
        return List.of(
                Component.translatable("tooltips.yagens_attributes.multishot_multibase", String.format("%.2f", ServerConfigs.MOD_COMMON_VIGILANTE_ARMAMENTS.get().floatValue() * modLevel)),
                Component.translatable("functions.yagens_attributes.vigilante_set.1", String.format("%.1f", ServerConfigs.MOD_SET_VIGILANTE.get().floatValue()))
        );
    }

    public VigilanteArmamentsToolMod() {
        super(5, "Nya", ModRarity.COMMON);
        this.baseCapacityCost = 4;
    }

    @Override
    public boolean isVigilanteSet() { return true; }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
