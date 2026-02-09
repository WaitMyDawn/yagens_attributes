package yagen.waitmydawn.item.mod.tool_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.config.ServerConfigs;

import java.util.List;

public class ScopeGalvanizedToolMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "scope_galvanized_tool_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel) {
        return List.of(
                Component.translatable("tooltips.yagens_attributes.critical_chance_multibaseneg", String.format("%.2f", ServerConfigs.MOD_LEGENDARY_GALVANIZED_SCOPE.get().floatValue() * modLevel)),
                Component.translatable("damagebonus.yagens_attributes.scope_tool_mod.1",
                        String.format("%.2f", ServerConfigs.MOD_RARE_SCOPE.get().floatValue() / 2 * modLevel),
                        String.format("%d", ServerConfigs.MOD_RARE_SCOPE_DURATION.get())),
                Component.translatable("killbonus.yagens_attributes.scope_galvanized_tool_mod.1",
                        String.format("%.2f", ServerConfigs.MOD_LEGENDARY_GALVANIZED_SCOPE_KILLBONUS.get().floatValue() * 100),
                        String.format("%d", ServerConfigs.MOD_RARE_SCOPE_DURATION.get()),
                        String.format("%d", ServerConfigs.MOD_LEGENDARY_GALVANIZED_SCOPE_STACK.get()))
        );
    }

    public ScopeGalvanizedToolMod() {
        super(10, "Cth", ModRarity.LEGENDARY);
        this.baseCapacityCost = 2;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
