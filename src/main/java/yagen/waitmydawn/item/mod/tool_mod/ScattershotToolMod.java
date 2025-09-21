package yagen.waitmydawn.item.mod.tool_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;

import java.util.List;

public class ScattershotToolMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "scattershot_tool_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel, LivingEntity player) {
        return List.of(
                Component.translatable("tooltips.yagens_attributes.multishot_multiply", String.format("%.1f", 60f * modLevel)),
                Component.translatable("tooltips.yagens_attributes.shoot_spread_add", 0.8f * modLevel),
                Component.translatable("functions.yagens_attributes.scattershot_tool_mod.1" )
        );
    }

    public ScattershotToolMod() {
        super(5, "Cth", ModRarity.RARE);
        this.baseCapacityCost = 10;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
