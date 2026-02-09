package yagen.waitmydawn.item.mod.tool_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.config.ServerConfigs;

import java.util.List;

public class MultishotGalvanizedToolMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "multishot_galvanized_tool_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel) {
        return List.of(
                Component.translatable("tooltips.yagens_attributes.multishot_multibase",
                        String.format("%.2f", ServerConfigs.MOD_LEGENDARY_GALVANIZED_MULTIPLY_SHOT.get().floatValue() * modLevel)),
                Component.translatable("killbonus.yagens_attributes.multishot_galvanized_tool_mod.1",
                        String.format("%.2f", ServerConfigs.MOD_LEGENDARY_GALVANIZED_MULTIPLY_SHOT_KILLBONUS.get().floatValue() * 100),
                        String.format("%d", ServerConfigs.MOD_LEGENDARY_GALVANIZED_MULTIPLY_SHOT_DURATION.get()),
                        String.format("%d", ServerConfigs.MOD_LEGENDARY_GALVANIZED_MULTIPLY_SHOT_STACK.get()))
        );
    }

    public MultishotGalvanizedToolMod() {
        super(10, "Cth", ModRarity.LEGENDARY);
        this.baseCapacityCost = 6;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
