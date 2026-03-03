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

public class HunterMunitionsToolMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "hunter_munitions_tool_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel, Player player) {
        return List.of(
                Component.translatable("functions.yagens_attributes.hunter_munitions_tool_mod.1", String.format("%.2f", ServerConfigs.MOD_UNCOMMON_HUNTER_MUNITIONS.get().floatValue() * modLevel)),
                Component.translatable("functions.yagens_attributes.hunter_set.1", String.format("%.1f", ServerConfigs.MOD_SET_HUNTER.get().floatValue()))
        );
    }

    public HunterMunitionsToolMod() {
        super(5, "Cth", ModRarity.UNCOMMON);
        this.baseCapacityCost = 4;
    }

    @Override
    public boolean isHunterSet() { return true; }


    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
