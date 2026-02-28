package yagen.waitmydawn.item.mod.tool_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.config.ServerConfigs;

import java.util.List;

public class DamageToolMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "damage_tool_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel) {
        return List.of(
                Component.translatable("tooltip.yagens_attributes.damage_multiply", String.format("%.2f", ServerConfigs.MOD_COMMON_DAMAGE.get().floatValue() * modLevel))
        );
    }

    public DamageToolMod() {
        super(5, "Cth", ModRarity.COMMON);
        this.baseCapacityCost = 2;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
