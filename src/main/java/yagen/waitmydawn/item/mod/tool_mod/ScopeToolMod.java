package yagen.waitmydawn.item.mod.tool_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;

import java.util.List;

public class ScopeToolMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "scope_tool_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel, LivingEntity player) {
        return List.of(
                Component.translatable("damagebonus.yagens_attributes.scope_tool_mod.1", 27f * modLevel)
        );
    }

    public ScopeToolMod() {
        super(5, "Cth", ModRarity.RARE);
        this.baseCapacityCost = 2;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
