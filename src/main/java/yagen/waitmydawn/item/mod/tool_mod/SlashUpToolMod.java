package yagen.waitmydawn.item.mod.tool_mod;

import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.*;
import yagen.waitmydawn.api.mods.ModRarity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;


import java.util.List;

public class SlashUpToolMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "slash_up_tool_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel, LivingEntity player) {
        return List.of(
                Component.translatable("tooltip.yagens_attributes.slash_addition", 15 * modLevel)
        );
    }

    public SlashUpToolMod() {
        super(10, "Cth", ModRarity.LEGENDARY);
        this.baseCapacityCost = 4;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
