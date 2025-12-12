package yagen.waitmydawn.item.mod.tool_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;

import java.util.List;

public class ScopeGalvanizedToolMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "scope_galvanized_tool_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel, LivingEntity player) {
        return List.of(
                Component.translatable("tooltips.yagens_attributes.critical_chance_multibaseneg", 1.5f * modLevel),
                Component.translatable("damagebonus.yagens_attributes.scope_tool_mod.1", 13.5f * modLevel),
                Component.translatable("killbonus.yagens_attributes.scope_galvanized_tool_mod.1")
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
