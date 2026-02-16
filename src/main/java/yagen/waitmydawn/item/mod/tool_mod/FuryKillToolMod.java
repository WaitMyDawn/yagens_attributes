package yagen.waitmydawn.item.mod.tool_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.config.ServerConfigs;

import java.util.List;

public class FuryKillToolMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "fury_kill_tool_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel) {
        return List.of(
                Component.translatable("killbonus.yagens_attributes.fury_kill_tool_mod.1",
                        String.format("%.2f", ServerConfigs.MOD_RARE_BERSERKER_FURY.get().floatValue() * modLevel),
                        ServerConfigs.MOD_RARE_BERSERKER_FURY_DURATION.get(),
                        ServerConfigs.MOD_RARE_BERSERKER_FURY_STACK.get())
        );
    }

    public FuryKillToolMod() {
        super(5, "Cth", ModRarity.RARE);
        this.baseCapacityCost = 4;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
